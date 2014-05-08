using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode
{
    public class PropertyInstance
    {
        public static PropertyInstance FindByTemplate(PropertyInfo propertyTemplate)
        {
            return FindByTemplate(propertyTemplate, false);
        }

        public static PropertyInstance FindByTemplate(PropertyInfo propertyTemplate, bool tryOnly)
        {
            return FindByTemplate(propertyTemplate.Name, tryOnly);
        }

        public static PropertyInstance FindByTemplate(PropertyInstance propertyTemplate, bool tryOnly)
        {
            return FindByTemplate(propertyTemplate.Name, tryOnly);
        }

        public static PropertyInstance FindByTemplate(Type declaringType, String propertyName, bool tryOnly)
        {
            PropertyInfo pi = declaringType.GetProperty(propertyName, BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Static | BindingFlags.Instance);
            if (pi != null)
            {
                return new PropertyInstance(pi);
            }
            if (tryOnly)
            {
                return null;
            }
            throw new ArgumentException("No property found on class hierarchy: " + propertyName + ". Start type: " + declaringType.FullName);
        }

        public static PropertyInstance FindByTemplate(String propertyName, bool tryOnly)
        {
            IBytecodeBehaviorState state = BytecodeBehaviorState.State;
            PropertyInstance pi = state.GetProperty(propertyName);
            if (pi != null)
            {
                return pi;
            }
            if (tryOnly)
            {
                return null;
            }
            throw new ArgumentException("No property found on class hierarchy: " + propertyName + ". Start type: " + state.NewType);
        }

        protected PropertyInfo property;

        protected MethodInstance getter;

        protected MethodInstance setter;

        protected readonly NewType owner;

        protected readonly String name;

        protected readonly NewType propertyType;

        protected readonly PropertyAttributes access;
        
        public PropertyInstance(PropertyInfo property)
        {
            this.property = property;
            this.owner = NewType.GetType(property.DeclaringType);
            this.access = property.Attributes;
            this.name = property.Name;
            this.getter = property.GetGetMethod() != null ? new MethodInstance(property.GetGetMethod()) : null;
            this.setter = property.GetSetMethod() != null ? new MethodInstance(property.GetSetMethod()) : null;
            propertyType = NewType.GetType(property.PropertyType);
        }

        public PropertyInstance(String propertyName, MethodInstance getter, MethodInstance setter)
            : this(BytecodeBehaviorState.State.NewType, PropertyAttributes.None, propertyName, getter, setter)
        {
            // Intended blank
        }

        public PropertyInstance(NewType owner, PropertyAttributes access, String propertyName, MethodInstance getter, MethodInstance setter)
        {
            this.owner = owner;
            this.access = access;
            this.name = propertyName;
            this.getter = getter;
            this.setter = setter;
            if (getter != null)
            {
                propertyType = getter.ReturnType;
            }
            else if (setter != null)
            {
                propertyType = setter.Parameters[0];
            }
            else
            {
                throw new ArgumentException("Either a getter or a setter must be specified");
            }
        }

        public NewType Owner
        {
            get
            {
                return owner;
            }
        }

        public PropertyAttributes Access
        {
            get
            {
                return access;
            }
        }

        public bool Configurable
        {
            get
            {
                return (property is PropertyBuilder);
            }
        }

        public void AddAnnotation(ConstructorInfo ci, params Object[] args)
        {
            if (!Configurable)
            {
                throw new ArgumentException();
            }
            ((PropertyBuilder)property).SetCustomAttribute(new CustomAttributeBuilder(ci, args));
        }

        public MethodInstance Getter
        {
            get
            {
                return getter;
            }
            set
            {
                if (property is PropertyBuilder && value != null && value.Method is MethodBuilder)
                {
                    ((PropertyBuilder)property).SetGetMethod((MethodBuilder)value.Method);
                    getter = value;
                }
                else
                {
                    throw new NotSupportedException();
                }
            }
        }

        public MethodInstance Setter
        {
            get
            {
                return setter;
            }
            set
            {
                if (property is PropertyBuilder && value != null && value.Method is MethodBuilder)
                {
                    ((PropertyBuilder)property).SetSetMethod((MethodBuilder)value.Method);
                    setter = value;
                }
                else
                {
                    throw new NotSupportedException();
                }
            }
        }

        public NewType PropertyType
        {
            get
            {
                return propertyType;
            }
        }

        public String Name
        {
            get
            {
                return name;
            }
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            if (Getter != null)
            {
                MethodAttributes access = Getter.Access;
                if (access.HasFlag(MethodAttributes.Public))
                {
                    sb.Append("public ");
                }
                else if (access.HasFlag(MethodAttributes.Family))
                {
                    sb.Append("protected ");
                }
                else if (access.HasFlag(MethodAttributes.Private))
                {
                    sb.Append("private ");
                }
                if (access.HasFlag(MethodAttributes.Static))
                {
                    sb.Append("static ");
                }
                if (access.HasFlag(MethodAttributes.Final))
                {
                    sb.Append("final ");
                }
                sb.Append(PropertyType.ClassName).Append(' ');
                sb.Append(Name);
                sb.Append(" { get; ");
                if (Setter == null)
                {
                    sb.Append('}');
                }
                else
                {
                    sb.Append("set; }");
                }
            }
            else if (Setter == null)
            {
                sb.Append(PropertyType.ClassName).Append(' ');
                sb.Append(Name);
                sb.Append("{}");
            }
            else
            {
                sb.Append(PropertyType.ClassName).Append(' ');
                sb.Append(Name);
                sb.Append("{ set; }");
            }
            return sb.ToString();
        }
    }
}