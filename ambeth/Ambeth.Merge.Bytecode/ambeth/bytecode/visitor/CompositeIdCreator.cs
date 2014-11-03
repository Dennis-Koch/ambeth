using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Mixin;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class CompositeIdCreator : ClassVisitor
    {
        public class CompositeIdValueResolveDelegate : IValueResolveDelegate
        {
            private readonly FieldInstance[] fields;

            public CompositeIdValueResolveDelegate(FieldInstance[] fields)
            {
                this.fields = fields;
            }

			public Object Invoke(String fieldName, Type enhancedType)
			{
				ITypeInfoItem[] members = new ITypeInfoItem[fields.Length];
                for (int a = fields.Length; a-- > 0; )
				{
					FieldInfo[] field = ReflectUtil.GetDeclaredFieldInHierarchy(enhancedType, fields[a].Name);
					members[a] = new FieldInfoItemASM(field[0]);
				}
				return members;
			}

			public Type ValueType
			{
                get
                {
				    return typeof(ITypeInfoItem[]);
                }
			}
        }

        public static readonly String FIELD_ACCESS_FLD = "s_fieldAccess";

        public static readonly String FIELD_INDEX_OF_MEMBERS_FLD = "s_fieldIndexOfMembers";

        public static readonly MethodInstance m_equalsCompositeId = new MethodInstance(null, typeof(CompositeIdCreator), typeof(bool), "EqualsCompositeId", typeof(ITypeInfoItem[]), typeof(Object), typeof(Object));

        public static readonly MethodInstance m_hashCodeCompositeId = new MethodInstance(null, typeof(CompositeIdCreator), typeof(int), "HashCodeCompositeId", typeof(ITypeInfoItem[]), typeof(Object));

        public static readonly MethodInstance m_toStringCompositeId = new MethodInstance(null, typeof(CompositeIdCreator), typeof(String), "ToStringCompositeId", typeof(ITypeInfoItem[]), typeof(Object));

        public static readonly MethodInstance m_toStringSbCompositeId = new MethodInstance(null, typeof(CompositeIdCreator), typeof(void), "ToStringSbCompositeId", typeof(ITypeInfoItem[]), typeof(Object), typeof(StringBuilder));

        public CompositeIdCreator(IClassVisitor cv)
            : base(new InterfaceAdder(cv, typeof(IPrintable)))
        {
            // Intended blank
        }

        public override void VisitEnd()
	    {
		    CompositeIdEnhancementHint context = State.GetContext<CompositeIdEnhancementHint>();
            Member[] idMembers = context.IdMembers;

		    CompositeIdMixin compositeIdTemplate = State.BeanContext.GetService<CompositeIdMixin>();

		    PropertyInstance p_compositeIdTemplate = ImplementAssignedReadonlyProperty("CompositeIdTemplate", compositeIdTemplate);

		    Type[] constructorTypes = new Type[idMembers.Length];
            FieldInstance[] fields = new FieldInstance[idMembers.Length];
		    // order does matter here (to maintain field order for debugging purpose on later objects)
		    for (int a = 0, size = idMembers.Length; a < size; a++)
		    {
                Member member = idMembers[a];
			    String fieldName = CompositeIdMember.FilterEmbeddedFieldName(member.Name);
			    constructorTypes[a] = member.RealType;
                fields[a] = new FieldInstance(FieldAttributes.Public | FieldAttributes.InitOnly, fieldName, constructorTypes[a]);
                ImplementField(fields[a], delegate(IFieldVisitor fv)
                {
                    fv.VisitAnnotation(typeof(PropertyAttribute).GetConstructor(Type.EmptyTypes));
                });
		    }

		    {
			    IMethodVisitor mg = VisitMethod(new ConstructorInstance(MethodAttributes.Public, constructorTypes));
			    mg.LoadThis();
			    mg.InvokeOnExactOwner(new ConstructorInstance(typeof(Object).GetConstructor(null)));
			    // order does matter here
			    for (int index = 0, size = fields.Length; index < size; index++)
			    {
				    mg.PutThisField(fields[index], delegate(IMethodVisitor mg2)
					    {
						    mg2.LoadArg(index);
					    }
				    );
			    }
			    mg.ReturnValue();
			    mg.EndMethod();
		    }            
		    PropertyInstance p_idMembers = ImplementAssignedReadonlyProperty("IdMembers", new CompositeIdValueResolveDelegate(fields));

		    {
			    // Implement boolean Object.equals(Object)
			    IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(Object), typeof(bool), "Equals", typeof(Object)));
			    // public static boolean CompositeIdCreator.equalsCompositeId(FieldAccess fa, int[] fieldIndexOfMembers, Object left, Object right)
                ImplementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_equalsCompositeId);
		    }
		    {
			    // Implement int Object.hashCode()
			    IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(Object), typeof(int), "GetHashCode"));
			    // public static int CompositeIdCreator.hashCodeCompositeId(FieldAccess fa, int[] fieldIndexOfMembers, Object compositeId)
                ImplementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_hashCodeCompositeId);
		    }
		    {
			    // Implement String Object.toString()
			    IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(Object), typeof(String), "ToString"));
			    // public static int CompositeIdCreator.toStringCompositeId(FieldAccess fa, int[] fieldIndexOfMembers, Object compositeId)
                ImplementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_toStringCompositeId);
		    }
		    {
			    // Implement void IPrintable.toString(StringBuilder)
			    IMethodVisitor mg = VisitMethod(new MethodInstance(null, typeof(IPrintable), typeof(void), "ToString", typeof(StringBuilder)));
			    // public static int CompositeIdCreator.toStringCompositeId(FieldAccess fa, int[] fieldIndexOfMembers, Object compositeId)
                ImplementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_toStringSbCompositeId);
		    }
		    base.VisitEnd();
	    }

        protected static void ImplementDefaultDelegatingMethod(IMethodVisitor mg, PropertyInstance p_compositeIdTemplate, PropertyInstance p_idMembers,
            MethodInstance delegatedMethod)
        {
            mg.CallThisGetter(p_compositeIdTemplate);
            mg.CallThisGetter(p_idMembers);
            mg.LoadThis();
            mg.LoadArgs();
            mg.InvokeVirtual(delegatedMethod);
            mg.ReturnValue();
            mg.EndMethod();
        }

        public static bool EqualsCompositeId(Object left, Object right)
        {
            if (left == null || right == null)
            {
                return false;
            }
            if (left == right)
            {
                return true;
            }
            if (!left.GetType().Equals(right.GetType()))
            {
                return false;
            }
            FieldInfo[] fields = left.GetType().GetFields();
            foreach (FieldInfo field in fields)
            {
                Object leftValue = field.GetValue(left);
                Object rightValue = field.GetValue(right);
                if (leftValue == null || rightValue == null)
                {
                    return false;
                }
                if (!leftValue.Equals(rightValue))
                {
                    return false;
                }
            }
            return true;
        }

        public static int HashCodeCompositeId(Object compositeId)
        {
            int hash = compositeId.GetType().GetHashCode();
            FieldInfo[] fields = compositeId.GetType().GetFields();
            foreach (FieldInfo field in fields)
            {
                Object value = field.GetValue(compositeId);
                if (value != null)
                {
                    hash ^= value.GetHashCode();
                }
            }
            return hash;
        }

        public static String ToStringCompositeId(Object compositeId)
        {
            StringBuilder sb = new StringBuilder();
            ToStringSbCompositeId(compositeId, sb);
            return sb.ToString();
        }

        public static void ToStringSbCompositeId(Object compositeId, StringBuilder sb)
        {
            // order does matter here
            FieldInfo[] fields = compositeId.GetType().GetFields();
            for (int a = 0, size = fields.Length; a < size; a++)
            {
                FieldInfo field = fields[a];
                Object value = field.GetValue(compositeId);
                if (a > 0)
                {
                    sb.Append('#');
                }
                if (value != null)
                {
                    StringBuilderUtil.AppendPrintable(sb, value);
                }
                else
                {
                    sb.Append("<null>");
                }
            }
        }
    }
}