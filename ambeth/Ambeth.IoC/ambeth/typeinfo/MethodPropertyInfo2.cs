using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Accessor;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class MethodPropertyInfo2 : AbstractPropertyInfo
    {
        protected static readonly Object[] EMPTY_ARGS = new Object[0];

        protected readonly AbstractAccessor accessor;

        public MethodInfo Getter { get; protected set; }

        public MethodInfo Setter { get; protected set; }

        public MethodPropertyInfo2(Type entityType, String propertyName, MethodInfo getter, MethodInfo setter, AbstractAccessor accessor)
            : base(entityType)
        {
            if (propertyName.Length == 0)
            {
                throw new Exception("Not a property method: " + entityType.Name + "." + getter.Name);
            }

            EntityType = entityType;
            Name = propertyName;
            this.accessor = accessor;

            if (getter != null)
		    {
			    ParamChecker.AssertTrue(!typeof(void).Equals(getter.ReturnType), "getter");
			    ParamChecker.AssertTrue(getter.GetParameters().Length == 0, "getter");
		    }
		    if (setter != null)
		    {
                ParamChecker.AssertTrue(setter.GetParameters().Length == 1, "setter");
		    }

            Getter = getter;
            Setter = setter;
            IsReadable = accessor.CanRead();
            IsWritable = accessor.CanWrite();

            Init();
        }

        protected override void Init()
        {
            if (EntityType == null)
            {
                throw new ArgumentException("No class given");
            }
            if (Getter == null && Setter == null)
            {
                throw new ArgumentException("No property methods (class is '" + EntityType + "')");
            }
            else if (Getter != null)
            {
                Type declaringClass = Getter.DeclaringType;
                PropertyType = Getter.ReturnType;
                ElementType = TypeInfoItemUtil.GetElementTypeUsingReflection(PropertyType, null);

                String nameLower = Name.ToLowerInvariant();
                FieldInfo[] fields = ReflectUtil.GetDeclaredFieldsInHierarchy(declaringClass);
                FieldInfo backingField = null, weakBackingField = null;
                String backingFieldName = "<" + Name + ">k__BackingField";

                for (int a = fields.Length; a-- > 0;)
                {
                    FieldInfo field = fields[a];
                    if (field.IsStatic)
                    {
                        continue;
                    }
                    String fieldName = field.Name.ToLowerInvariant();
                    if (fieldName.Equals(nameLower) || field.Name.Equals(backingFieldName))
                    {
                        backingField = field;
                        break;
                    }
                    else if (fieldName.EndsWith(nameLower))
                    {
                        if (weakBackingField != null)
                        {
                            weakBackingField = null;
                            break;
                        }
                        weakBackingField = field;
                    }
                }
                if (backingField == null)
                {
                    backingField = weakBackingField;
                }
                BackingField = backingField;
                if (backingField != null)
                {
                    //PutAnnotations(backingField);
                }
                PutAnnotations(Getter);
                if (Setter != null)
                {
                    if (Setter.GetParameters().Length != 1 || !Setter.GetParameters()[0].ParameterType.Equals(PropertyType))
                    {
                        throw new Exception("Misfitting property methods for property '" + Name + "' on class '" + EntityType.Name + "'");
                    }
                    PutAnnotations(Setter);
                }
            }
            else
            {
                Type declaringClass = Setter.DeclaringType;
                FieldInfo[] fields = ReflectUtil.GetDeclaredFieldsInHierarchy(declaringClass);
                for (int a = fields.Length; a-- > 0; )
                {
                    FieldInfo field = fields[a];
                    if (field.IsInitOnly || field.IsStatic)
                    {
                        continue;
                    }
                    String fieldName = field.Name;
                    if (fieldName.EndsWith(Name))
                    {
                        if (BackingField != null)
                        {
                            BackingField = null;
                            break;
                        }
                        BackingField = field;
                    }
                }
                PropertyType = Setter.GetParameters()[0].ParameterType;
                ElementType = TypeInfoItemUtil.GetElementTypeUsingReflection(PropertyType, null);
                PutAnnotations(Setter);
            }
            DeclaringType = BackingField != null ? BackingField.DeclaringType : Getter != null ? Getter.DeclaringType : Setter.DeclaringType;
            
            base.Init();
        }

        public override Object GetValue(Object obj)
        {
            return accessor.GetValue(obj);
        }

        public override void SetValue(Object obj, Object value)
        {
            accessor.SetValue(obj, value);
        }
    }
}