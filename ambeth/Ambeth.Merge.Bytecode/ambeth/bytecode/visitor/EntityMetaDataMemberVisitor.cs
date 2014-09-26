using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EntityMetaDataMemberVisitor : ClassVisitor
    {
        protected static readonly MethodInstance template_m_canRead = new MethodInstance(null, typeof(Member), typeof(bool), "get_CanRead");

        protected static readonly MethodInstance template_m_canWrite = new MethodInstance(null, typeof(Member), typeof(bool), "get_CanWrite");

        protected static readonly MethodInstance template_m_getAttribute = new MethodInstance(null, typeof(Member), typeof(Attribute), "GetAnnotation", typeof(Type));

        protected static readonly MethodInstance template_m_getDeclaringType = new MethodInstance(null, typeof(Member), typeof(Type), "get_DeclaringType");

        protected static readonly MethodInstance template_m_getNullEquivalentValue = new MethodInstance(null, typeof(Member), typeof(Object), "get_NullEquivalentValue");

        protected static readonly MethodInstance template_m_getName = new MethodInstance(null, typeof(Member), typeof(String), "get_Name");

        protected static readonly MethodInstance template_m_getEntityType = new MethodInstance(null, typeof(Member), typeof(Type), "get_EntityType");

        protected static readonly MethodInstance template_m_getElementType = new MethodInstance(null, typeof(Member), typeof(Type), "get_ElementType");

        protected static readonly MethodInstance template_m_getRealType = new MethodInstance(null, typeof(Member), typeof(Type), "get_RealType");

        protected static readonly MethodInstance template_m_isToMany = new MethodInstance(null, typeof(Member), typeof(bool), "get_IsToMany");

        protected static readonly MethodInstance template_m_getValue = new MethodInstance(null, typeof(Member), typeof(Object), "GetValue", typeof(Object));

        public static readonly MethodInstance template_m_getValueWithFlag = new MethodInstance(null, typeof(Member), typeof(Object), "GetValue", typeof(Object),
                typeof(bool));

        protected static readonly MethodInstance template_m_setValue = new MethodInstance(null, typeof(Member), typeof(void), "SetValue", typeof(Object), typeof(Object));

        protected readonly Type entityType;

        protected readonly String memberName;

        protected IBytecodeEnhancer bytecodeEnhancer;

        protected IEntityMetaDataProvider entityMetaDataProvider;

        protected IPropertyInfoProvider propertyInfoProvider;

        public EntityMetaDataMemberVisitor(IClassVisitor cv, Type entityType, String memberName, IBytecodeEnhancer bytecodeEnhancer,
                IEntityMetaDataProvider entityMetaDataProvider, IPropertyInfoProvider propertyInfoProvider)
            : base(cv)
        {
            this.entityType = entityType;
            this.memberName = memberName;
            this.entityMetaDataProvider = entityMetaDataProvider;
            this.propertyInfoProvider = propertyInfoProvider;
        }

        public override void VisitEnd()
        {
            IPropertyInfo[] propertyPath = MemberTypeProvider.BuildPropertyPath(entityType, memberName, propertyInfoProvider);
            ImplementCanRead(propertyPath);
            ImplementCanWrite(propertyPath);
            ImplementGetAttribute(propertyPath);
            ImplementGetDeclaringType(propertyPath);
            ImplementGetName(propertyPath);
            ImplementGetNullEquivalentValue(propertyPath);
            ImplementGetEntityType(propertyPath);
            ImplementGetElementType(propertyPath);
            ImplementGetRealType(propertyPath);
            ImplementIsToMany(propertyPath);
            ImplementGetValue(propertyPath);
            ImplementSetValue(propertyPath);
            base.VisitEnd();
        }

        protected void ImplementCanRead(IPropertyInfo[] property)
        {
            IMethodVisitor mv = VisitMethod(template_m_canRead);
            mv.Push(property[property.Length - 1].IsReadable);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementCanWrite(IPropertyInfo[] property)
        {
            IMethodVisitor mv = VisitMethod(template_m_canWrite);
            mv.Push(property[property.Length - 1].IsWritable);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetNullEquivalentValue(IPropertyInfo[] property)
        {
            IMethodVisitor mv = VisitMethod(template_m_getNullEquivalentValue);
            Type propertyType = property[property.Length - 1].PropertyType;
            mv.PushNullOrZero(propertyType);
            if (propertyType.IsPrimitive)
            {
                mv.Box(propertyType);
            }
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetEntityType(IPropertyInfo[] property)
        {
            IMethodVisitor mv = VisitMethod(template_m_getEntityType);
            Type entityType = this.entityType;
		    IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(entityType, true);
		    if (metaData != null)
		    {
			    entityType = metaData.EntityType;
		    }
		    mv.Push(entityType);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetElementType(IPropertyInfo[] property)
        {
            IMethodVisitor mv = VisitMethod(template_m_getElementType);
            mv.Push(property[property.Length - 1].ElementType);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetRealType(IPropertyInfo[] property)
        {
            IMethodVisitor mv = VisitMethod(template_m_getRealType);
            mv.Push(property[property.Length - 1].PropertyType);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetAttribute(IPropertyInfo[] property)
        {
            HashMap<Type, Attribute> typeToAttributeMap = new HashMap<Type, Attribute>();
            Attribute[] annotations = property[property.Length - 1].GetAnnotations();
            foreach (Attribute annotation in annotations)
            {
                typeToAttributeMap.Put(annotation.GetType(), annotation);
            }
            FieldInstance f_typeToAttributeMap = ImplementStaticAssignedField("sf__typeToAttributeMap", typeToAttributeMap);
            IMethodVisitor mv = VisitMethod(template_m_getAttribute);
            mv.GetThisField(f_typeToAttributeMap);
            mv.LoadArg(0);
            mv.InvokeVirtual(new MethodInstance(null, typeof(HashMap<Type, Attribute>), typeof(Attribute), "Get", typeof(Type)));
            //		mv.CheckCast(typeof(Attribute));
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetDeclaringType(IPropertyInfo[] property)
        {
            IMethodVisitor mv = VisitMethod(template_m_getDeclaringType);
            mv.Push(entityType);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetName(IPropertyInfo[] property)
        {
            StringBuilder compositeName = new StringBuilder();
            for (int a = 0, size = property.Length; a < size; a++)
            {
                if (a > 0)
                {
                    compositeName.Append('.');
                }
                compositeName.Append(property[a].Name);
            }
            IMethodVisitor mv = VisitMethod(template_m_getName);
            mv.Push(compositeName.ToString());
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetValue(IPropertyInfo[] propertyPath)
        {
            for (int a = 0, size = propertyPath.Length; a < size; a++)
            {
                IPropertyInfo property = propertyPath[a];
                if (property is MethodPropertyInfo && ((MethodPropertyInfo)property).Getter == null)
                {
                    throw new Exception("Property not readable: " + property.EntityType.FullName + "." + property.Name);
                }
            }
            ImplementGetValueIntern(template_m_getValue, propertyPath);
            ImplementGetValueIntern(template_m_getValueWithFlag, propertyPath);
        }

        protected void ImplementGetValueIntern(MethodInstance template_m_getValue, IPropertyInfo[] propertyPath)
        {
            IMethodVisitor mv = VisitMethod(template_m_getValue);
            
            Type declaringType = propertyPath[0].EntityType;
            Label l_finish = mv.NewLabel();
            mv.LoadArg(0);
            mv.CheckCast(declaringType);
            for (int a = 0, size = propertyPath.Length - 1; a < size; a++)
            {
                InvokeGetProperty(mv, propertyPath[a]);
                mv.Dup();
                mv.IfNull(l_finish);
            }
            IPropertyInfo lastProperty = propertyPath[propertyPath.Length - 1];
            InvokeGetProperty(mv, lastProperty);
            if (lastProperty.PropertyType.IsPrimitive)
            {
                Type pType = lastProperty.PropertyType;
                LocalVariableInfo loc_value = mv.NewLocal(pType);
                mv.StoreLocal(loc_value);
                mv.LoadLocal(loc_value);
                Label l_valueIsNonZero = mv.NewLabel();

                mv.IfZCmp(pType, CompareOperator.NE, l_valueIsNonZero);

                if (mv.Method.Parameters.Length == 2)
                {
                    // check null-equi flag

                    Label l_nullAllowed = mv.NewLabel();

                    mv.LoadArg(1);
                    mv.IfZCmp(CompareOperator.EQ, l_nullAllowed);
                    mv.PushNullOrZero(pType);
                    if (!mv.Method.ReturnType.Type.IsValueType)
                    {
                        mv.ValueOf(pType);
                    }
                    mv.ReturnValue();

                    mv.Mark(l_nullAllowed);
                }
                mv.PushNullOrZero(mv.Method.ReturnType);
                mv.ReturnValue();

                mv.Mark(l_valueIsNonZero);
                mv.LoadLocal(loc_value);
                mv.ValueOf(pType);
            }
            else if (lastProperty.PropertyType.IsValueType)
            {
                Type pType = lastProperty.PropertyType;

                MethodInfo m_hasValue = pType.GetMethod("get_HasValue");
                if (m_hasValue != null)
                {
                    LocalVariableInfo loc_value = mv.NewLocal(pType);
                    mv.StoreLocal(loc_value);
                    mv.LoadLocal(loc_value);

                    MethodInfo m_getValue = pType.GetMethod("get_Value");
                    LocalVariableInfo loc_realValue = mv.NewLocal(m_getValue.ReturnType);
                    Label l_hasNoValue = mv.NewLabel();
                    Label l_valueIsNonZero = mv.NewLabel();

                    mv.InvokeOnExactOwner(m_hasValue);
                    mv.IfZCmp(CompareOperator.EQ, l_hasNoValue);
                    mv.LoadLocal(loc_value);
                    mv.InvokeOnExactOwner(m_getValue);
                    mv.StoreLocal(loc_realValue);
                    mv.LoadLocal(loc_realValue);
                    mv.IfZCmp(CompareOperator.EQ, l_hasNoValue);
                    mv.LoadLocal(loc_realValue);
                    if (!mv.Method.ReturnType.Type.IsValueType)
                    {
                        mv.ValueOf(m_getValue.ReturnType);
                    }
                    mv.ReturnValue();

                    mv.Mark(l_hasNoValue);

                    if (mv.Method.Parameters.Length == 2)
                    {
                        Label l_nullEquivalentValueAllowed = mv.NewLabel();
                        // check null-equi flag
                        mv.LoadArg(1);
                        mv.IfZCmp(CompareOperator.NE, l_nullEquivalentValueAllowed);
                        mv.PushNull();
                        mv.ReturnValue();

                        mv.Mark(l_nullEquivalentValueAllowed);
                    }
                    mv.PushNullOrZero(m_getValue.ReturnType);
                    if (!mv.Method.ReturnType.Type.IsValueType)
                    {
                        mv.ValueOf(m_getValue.ReturnType);
                    }
                }
                else
                {
                    mv.Box(pType);
                }
            }
            mv.Mark(l_finish);
            mv.ReturnValue();
            mv.EndMethod();
        }

        public int? GetId()
        {
            return 5;
        }

        public Object GetValue(Object obj)
        {
            return null;
        }

        public V GetValue<V>(Object obj)
        {
            return (V)GetValue(obj);
        }

        public Object GetValue(Object value, bool value2)
        {
            int? id = ((EntityMetaDataMemberVisitor)value).GetId();
            if (id.HasValue)
            {
                return id.Value;
            }
            if (value2)
            {
                return 0;
            }
            return null;
        }


        protected void ImplementSetValue(IPropertyInfo[] propertyPath)
        {
            IMethodVisitor mv = VisitMethod(template_m_setValue);

            for (int a = 0, size = propertyPath.Length - 1; a < size; a++)
            {
                IPropertyInfo property = propertyPath[a];
                if (property is MethodPropertyInfo && ((MethodPropertyInfo)property).Getter == null)
                {
                    throw new Exception("Property not readable: " + property.EntityType.FullName + "." + property.Name);
                }
            }
            IPropertyInfo lastProperty = propertyPath[propertyPath.Length - 1];
            if (lastProperty is MethodPropertyInfo && ((MethodPropertyInfo)lastProperty).Setter == null)
            {
                throw new Exception("Property not writable: " + lastProperty.EntityType.FullName + "." + lastProperty.Name);
            }
            mv.LoadArg(0);
            mv.CheckCast(propertyPath[0].EntityType);

            for (int a = 0, size = propertyPath.Length - 1; a < size; a++)
            {
                InvokeGetProperty(mv, propertyPath[a]);
                // mv.Dup();
                // Label l_pathIsNonNull = mv.NewLabel();
                // mv.ifNonNull(l_pathIsNonNull);
                //
                // mv.pop(); // remove remaining null of embedded object from stack, now the cached parent object is on the stack
                // mv.Dup(); // cache parent object again
                //
                // mv.callThisGetter(p_embeddedMemberTemplate);
                // mv.Push(propertyPath[a]..PropertyType); // embeddedType
                // mv.Push(entityType); // entityType
                // mv.// parentObject
                // mv.Push(sb.toString()); // memberPath
                // mv.InvokeVirtual(template_m_createEmbeddedObject);
                //
                // mv.mark(l_pathIsNonNull);
            }

            mv.LoadArg(1);
            Type lastPropertyType = lastProperty.PropertyType;
            if (lastProperty.PropertyType.IsPrimitive)
            {
                Type pType = lastProperty.PropertyType;
                Label l_valueIsNonNull = mv.NewLabel();
                Label l_valueIsValid = mv.NewLabel();

                mv.IfNonNull(l_valueIsNonNull);
                mv.PushNullOrZero(pType);
                mv.GoTo(l_valueIsValid);

                mv.Mark(l_valueIsNonNull);
                mv.LoadArg(1);
                mv.Unbox(pType);
                mv.Mark(l_valueIsValid);
            }
            else
            {
                mv.CheckCast(lastPropertyType);
            }
            InvokeSetProperty(mv, lastProperty);
            mv.ReturnValue();

            mv.EndMethod();
        }

        protected void InvokeGetProperty(IMethodVisitor mv, IPropertyInfo property)
        {
            if (property is MethodPropertyInfo)
            {
                MethodInfo method = ((MethodPropertyInfo)property).Getter;
                if (method.DeclaringType.IsInterface)
                {
                    mv.InvokeInterface(new MethodInstance(method));
                }
                else
                {
                    mv.InvokeVirtual(new MethodInstance(method));
                }
            }
            else
            {
                FieldInfo field = ((FieldPropertyInfo)property).BackingField;
                mv.GetField(new FieldInstance(field));
            }
        }

        protected void InvokeSetProperty(IMethodVisitor mv, IPropertyInfo property)
        {
            if (property is MethodPropertyInfo)
            {
                MethodInfo method = ((MethodPropertyInfo)property).Setter;
                if (method.DeclaringType.IsInterface)
                {
                    mv.InvokeInterface(new MethodInstance(method));
                }
                else
                {
                    mv.InvokeVirtual(new MethodInstance(method));
                }
            }
            else
            {
                FieldInfo field = ((FieldPropertyInfo)property).BackingField;
                mv.PutField(new FieldInstance(field));
            }
        }

        protected void ImplementIsToMany(IPropertyInfo[] propertyPath)
        {
            IMethodVisitor mv = VisitMethod(template_m_isToMany);
            mv.Push(ListUtil.IsCollection(propertyPath[propertyPath.Length - 1].PropertyType));
            mv.ReturnValue();
            mv.EndMethod();
        }
    }
}