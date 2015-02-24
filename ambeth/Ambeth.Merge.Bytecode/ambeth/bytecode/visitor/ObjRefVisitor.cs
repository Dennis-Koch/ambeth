using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Mixin;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class ObjRefVisitor : ClassVisitor
    {
        public static readonly Type templateType = typeof(ObjRefMixin);

	    protected static readonly String templatePropertyName = templateType.Name;

        private static readonly ConstructorInstance c_stringBuilder = new ConstructorInstance(typeof(StringBuilder).GetConstructor(Type.EmptyTypes));

        private static readonly PropertyInstance template_p_realType = PropertyInstance.FindByTemplate(typeof(IObjRef), "RealType", typeof(Type), false);

        private static readonly PropertyInstance template_p_idIndex = PropertyInstance.FindByTemplate(typeof(IObjRef), "IdNameIndex", typeof(sbyte), false);

        private static readonly PropertyInstance template_p_id = PropertyInstance.FindByTemplate(typeof(IObjRef), "Id", typeof(Object), false);

        private static readonly PropertyInstance template_p_version = PropertyInstance.FindByTemplate(typeof(IObjRef), "Version", typeof(Object), false);

       	private static readonly MethodInstance template_m_equals = new MethodInstance(null, typeof(Object), typeof(bool), "Equals", typeof(Object));

	    private static readonly MethodInstance template_m_hashCode = new MethodInstance(null, typeof(Object), typeof(int), "GetHashCode");

        private static readonly MethodInstance template_m_toString = new MethodInstance(null, typeof(Object), typeof(String), "ToString");

        private static readonly MethodInstance template_m_toStringSb = new MethodInstance(null, typeof(IPrintable), typeof(void), "ToString", typeof(StringBuilder));

        private static readonly MethodInstance m_objRef_equals = new MethodInstance(null, templateType, typeof(bool), "ObjRefEquals", typeof(IObjRef), typeof(Object));

	    private static readonly MethodInstance m_objRef_hashCode = new MethodInstance(null, templateType, typeof(int), "ObjRefHashCode", typeof(IObjRef));

	    private static readonly MethodInstance m_objRef_toStringSb = new MethodInstance(null, templateType, typeof(void), "ObjRefToString", typeof(IObjRef),
			typeof(StringBuilder));

        public static PropertyInstance GetObjRefTemplatePI(IClassVisitor cv)
        {
            Object bean = State.BeanContext.GetService(templateType);
            PropertyInstance pi = State.GetProperty(templatePropertyName, NewType.GetType(bean.GetType()));
            if (pi != null)
            {
                return pi;
            }
            return cv.ImplementAssignedReadonlyProperty(templatePropertyName, bean);
        }

        public static PropertyInstance GetConversionHelperPI(IClassVisitor cv)
	    {
		    Object bean = State.BeanContext.GetService<IConversionHelper>();
		    PropertyInstance pi = State.GetProperty("ConversionHelper", NewType.GetType(bean.GetType()));
		    if (pi != null)
		    {
			    return pi;
		    }
		    return cv.ImplementAssignedReadonlyProperty("ConversionHelper", bean);
	    }

        protected readonly IEntityMetaData metaData;

        protected readonly int idIndex;

        public ObjRefVisitor(IClassVisitor cv, IEntityMetaData metaData, int idIndex)
            : base(new InterfaceAdder(cv, typeof(IObjRef)))
        {
            this.metaData = metaData;
            this.idIndex = idIndex;
        }

        public override void VisitEnd()
        {
            ImplementRealType();
            ImplementIdIndex();
            PropertyInstance p_id = ImplementId();
            PropertyInstance p_version = ImplementVersion();
            ImplementDefaultConstructor();
            ImplementIdVersionConstructor(p_id, p_version);
            ImplementToString();
            ImplementEquals();
            ImplementHashCode();

            base.VisitEnd();
        }

        protected void ImplementDefaultConstructor()
        {
            IMethodVisitor mv = VisitMethod(ConstructorInstance.defaultConstructor);
            mv.LoadThis();
            mv.InvokeSuperOfCurrentMethod();
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementIdVersionConstructor(PropertyInstance p_id, PropertyInstance p_version)
        {
            ConstructorInfo superConstructor = State.CurrentType.GetConstructor(Type.EmptyTypes);
            ConstructorInstance ci_super = new ConstructorInstance(superConstructor);

            IMethodVisitor mv = VisitMethod(new ConstructorInstance(MethodAttributes.Public, typeof(Object), typeof(Object)));
            mv.LoadThis();
            mv.InvokeConstructor(ci_super);
            mv.CallThisSetter(p_id, delegate(IMethodVisitor mg)
                {
                    mg.LoadArg(0);
                });
            mv.CallThisSetter(p_version, delegate(IMethodVisitor mg)
                {
                    mg.LoadArg(1);
                });
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementRealType()
        {
            ImplementProperty(template_p_realType, delegate(IMethodVisitor mg)
                {
                    mg.Push(metaData.EntityType);
                    mg.ReturnValue();
                }, delegate(IMethodVisitor mg)
                {
                    mg.ThrowException(typeof(NotSupportedException), "Property is read-only");
                    mg.ReturnValue();
                });
        }

        protected void ImplementIdIndex()
        {
            ImplementProperty(template_p_idIndex, delegate(IMethodVisitor mg)
                {
                    mg.Push(idIndex);
                    mg.ReturnValue();
                }, delegate(IMethodVisitor mg)
                {
                    mg.ThrowException(typeof(NotSupportedException), "Property is read-only");
                    mg.ReturnValue();
                });
        }

        protected PropertyInstance ImplementPotentialPrimitive(PropertyInstance property, Member member)
        {
            if (member == null)
            {
                return ImplementProperty(property, delegate(IMethodVisitor mg)
                    {
                        mg.PushNull();
                        mg.ReturnValue();
                    }, delegate(IMethodVisitor mg)
                    {
                        Label l_isNull = mg.NewLabel();

                        mg.LoadArg(0);
                        mg.IfNull(l_isNull);
                        mg.ThrowException(typeof(NotSupportedException), "Property is read-only");
                        mg.Mark(l_isNull);
                        mg.ReturnValue();
                    });
            }
            PropertyInstance p_conversionHelper = GetConversionHelperPI(this);

		    MethodInstance m_convertValueToType = new MethodInstance(ReflectUtil.GetDeclaredMethod(false, typeof(IConversionHelper), typeof(Object),
				"ConvertValueToType", typeof(Type), typeof(Object)));

            Type type = member.RealType;
            FieldInstance field = ImplementField(new FieldInstance(FieldAttributes.Private, "f_" + property.Name, type));
            return ImplementProperty(property, delegate(IMethodVisitor mg)
                {
                    if (member.RealType.IsValueType)
                    {
                        Label l_isNull = mg.NewLabel();
                        LocalVariableInfo loc_value = mg.NewLocal(field.Type);

                        mg.GetThisField(field);
                        mg.StoreLocal(loc_value);

                        mg.LoadLocal(loc_value);
                        mg.IfZCmp(field.Type, CompareOperator.EQ, l_isNull);

                        mg.LoadLocal(loc_value);
                        mg.Box(type);
                        mg.ReturnValue();

                        mg.Mark(l_isNull);
                        mg.PushNull();
                        mg.ReturnValue();
                    }
                    else
                    {
                        mg.GetThisField(field);
                        mg.ReturnValue();
                    }
                }, delegate(IMethodVisitor mg)
                {
                    mg.PutThisField(field, delegate(IMethodVisitor mg2)
                        {
                            Label l_isNull = mg2.NewLabel();
                            Label l_finish = mg2.NewLabel();

                            mg2.LoadArg(0);
                            mg2.IfNull(l_isNull);

                            mg.CallThisGetter(p_conversionHelper);
                            mg.Push(type);
                            mg.LoadArg(0);
                            mg.InvokeVirtual(m_convertValueToType);

                            mg2.Unbox(type);
                            mg2.GoTo(l_finish);

                            mg2.Mark(l_isNull);
                            mg2.PushNullOrZero(field.Type);

                            mg2.Mark(l_finish);
                        });
                    mg.ReturnValue();
                });
        }

        protected PropertyInstance ImplementId()
        {
            return ImplementPotentialPrimitive(template_p_id, metaData.GetIdMemberByIdIndex((sbyte)idIndex));
        }

        protected PropertyInstance ImplementVersion()
        {
            return ImplementPotentialPrimitive(template_p_version, metaData.VersionMember);
        }

        protected void ImplementToString()
        {
            PropertyInstance p_objRefTemplate = GetObjRefTemplatePI(this);

            MethodInstance methodSb;
		    {
			    methodSb = MethodInstance.FindByTemplate(template_m_toStringSb, true);
			    if (methodSb == null || methodSb.Access.HasFlag(MethodAttributes.Abstract))
			    {
				    IMethodVisitor mg = VisitMethod(template_m_toStringSb);
				    mg.CallThisGetter(p_objRefTemplate);
				    mg.LoadThis();
				    mg.LoadArgs();
				    mg.InvokeVirtual(m_objRef_toStringSb);
				    mg.ReturnValue();
				    mg.EndMethod();
				    methodSb = mg.Method;
			    }
		    }
		    {
			    MethodInstance method = MethodInstance.FindByTemplate(template_m_toString, true);
			    if (method == null || NewType.GetType(typeof(Object)).Equals(method.Owner) || methodSb.Access.HasFlag(MethodAttributes.Abstract))
			    {
				    IMethodVisitor mg = VisitMethod(template_m_toString);
				    LocalVariableInfo loc_sb = mg.NewLocal(typeof(StringBuilder));
				    mg.LoadThis();
                    mg.NewInstance(c_stringBuilder, null);
				    mg.StoreLocal(loc_sb);
				    mg.LoadLocal(loc_sb);
				    mg.InvokeVirtual(methodSb);
				    mg.LoadLocal(loc_sb);
				    mg.InvokeVirtual(new MethodInstance(null, typeof(StringBuilder), typeof(String), "ToString"));
				    mg.ReturnValue();
				    mg.EndMethod();
			    }
		    }
        }

	    protected void ImplementEquals()
	    {
		    PropertyInstance p_objRefTemplate = GetObjRefTemplatePI(this);

		    MethodInstance method = MethodInstance.FindByTemplate(template_m_equals, true);
            if (method == null || method.Access.HasFlag(MethodAttributes.Abstract))
		    {
			    IMethodVisitor mg = VisitMethod(template_m_equals);
			    mg.CallThisGetter(p_objRefTemplate);
			    mg.LoadThis();
			    mg.LoadArgs();
			    mg.InvokeVirtual(m_objRef_equals);
			    mg.ReturnValue();
			    mg.EndMethod();
		    }
	    }

	    protected void ImplementHashCode()
	    {
		    PropertyInstance p_objRefTemplate = GetObjRefTemplatePI(this);

		    MethodInstance method = MethodInstance.FindByTemplate(template_m_hashCode, true);
            if (method == null || method.Access.HasFlag(MethodAttributes.Abstract))
		    {
                IMethodVisitor mg = VisitMethod(template_m_hashCode);
			    mg.CallThisGetter(p_objRefTemplate);
			    mg.LoadThis();
			    mg.LoadArgs();
			    mg.InvokeVirtual(m_objRef_hashCode);
			    mg.ReturnValue();
			    mg.EndMethod();
		    }
	    }
    }
}