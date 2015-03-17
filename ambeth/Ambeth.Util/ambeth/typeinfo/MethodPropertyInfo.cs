using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Exceptions;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class MethodPropertyInfo : AbstractPropertyInfo
    {
        protected static readonly Object[] EMPTY_ARGS = new Object[0];

        public MethodInfo Getter { get; protected set; }

        public MethodInfo Setter { get; protected set; }

        public MethodPropertyInfo(Type entityType, String propertyName, MethodInfo getter, MethodInfo setter)
            : base(entityType)
        {
            if (propertyName.Length == 0)
            {
                throw new Exception("Not a property method: " + entityType.Name + "." + getter.Name);
            }

            EntityType = entityType;
            Name = propertyName;

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
            IsWritable = setter != null && setter.IsPublic;
            IsReadable = getter != null && getter.IsPublic;

            Init();
        }

        protected void RefreshDeclaringType()
	    {
		    Type fieldDC = BackingField != null ? BackingField.DeclaringType : null;
            Type getterDC = Getter != null ? Getter.DeclaringType : null;
            Type setterDC = Setter != null ? Setter.DeclaringType : null;
            Type mostSpecificDC = fieldDC;
		    if (mostSpecificDC == null)
		    {
			    mostSpecificDC = getterDC;
		    }
		    else if (getterDC != null)
		    {
			    mostSpecificDC = getterDC.IsAssignableFrom(mostSpecificDC) ? mostSpecificDC : getterDC;
		    }
		    if (mostSpecificDC == null)
		    {
			    mostSpecificDC = setterDC;
		    }
		    else if (setterDC != null)
		    {
                mostSpecificDC = setterDC.IsAssignableFrom(mostSpecificDC) ? mostSpecificDC : setterDC;
		    }
		    DeclaringType = mostSpecificDC;
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
                FieldInfo backingField = null;
                FieldInfo weakBackingField = null;
                String backingFieldName = "<" + Name + ">k__BackingField";

                for (int a = fields.Length; a-- > 0; )
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
                    if (Setter.GetParameters().Length != 1 || !PropertyType.IsAssignableFrom(Setter.GetParameters()[0].ParameterType))
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
            RefreshDeclaringType();
            base.Init();
        }

        public override void RefreshAccessors(Type realType)
	    {
            base.RefreshAccessors(realType);
		    Getter = ReflectUtil.GetDeclaredMethod(true, realType, PropertyType, "get_" + Name);
		    Setter = ReflectUtil.GetDeclaredMethod(true, realType, null, "set_" + Name, PropertyType);
            IsWritable = this.Setter != null && !Setter.IsPrivate;
		    IsReadable = this.Getter != null && !Getter.IsPrivate;
            RefreshDeclaringType();
	    }

        public override Object GetValue(Object obj)
        {
            if (Getter == null)
            {
                return null;
            }
            try
            {
                return Getter.Invoke(obj, EMPTY_ARGS);
            }
            catch (Exception e)
            {
                throw RuntimeExceptionUtil.Mask(e, "Error occured while calling '" + Getter + "' on object '" + obj + "' of type '" + obj.GetType().ToString()
                        + "'");
            }
        }

        public override void SetValue(Object obj, Object value)
        {
            if (Setter == null)
            {
                throw new NotSupportedException("No setter configure for property " + Name);
            }
            Object[] args = { value };
            try
            {
                Setter.Invoke(obj, args);
            }
            catch (Exception e)
            {
                throw RuntimeExceptionUtil.Mask(e, "Error occured while calling '" + Setter + "' on object '" + obj + "' of type '" + obj.GetType().ToString()
                        + "' with argument '" + value + "'");
            }
        }
    }
}