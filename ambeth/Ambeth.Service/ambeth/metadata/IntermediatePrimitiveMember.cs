using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class IntermediatePrimitiveMember : PrimitiveMember
    {
        protected readonly String propertyName;

        protected readonly Type type;

        protected readonly Type realType;

        protected readonly Type elementType;

        public IntermediatePrimitiveMember(Type type, Type realType, Type elementType, String propertyName)
            : base(type, null)
        {
            this.type = type;
            this.realType = realType;
            this.elementType = elementType;
            this.propertyName = propertyName;
        }

        public override bool CanRead
        {
            get
            {
                return true;
            }
        }

        public override bool CanWrite
        {
            get
            {
                return true;
            }
        }

        public override String Name
        {
            get
            {
                return propertyName;
            }
        }

        public override Type DeclaringType
        {
            get
            {
                return type;
            }
        }

        public override Type RealType
        {
            get
            {
                return realType;
            }
        }

        public override Attribute GetAnnotation(Type annotationType)
        {
            throw CreateException();
        }

        protected Exception CreateException()
        {
            return new NotSupportedException("This in an intermediate member which works only as a stub for a later bytecode-enhanced member");
        }

        public override bool TechnicalMember
        {
            get
            {
                throw CreateException();
            }
        }

        public override Object NullEquivalentValue
        {
            get
            {
                throw CreateException();
            }
        }

        public override bool IsToMany
        {
            get
            {
                throw CreateException();
            }
        }

        public override Type ElementType
        {
            get
            {
                return elementType;
            }
        }

        public override Type EntityType
        {
            get
            {
                return type;
            }
        }

        public override Object GetValue(Object obj)
        {
            throw CreateException();
        }

        public override Object GetValue(Object obj, bool allowNullEquivalentValue)
        {
            throw CreateException();
        }

        public override void SetValue(Object obj, Object value)
        {
            throw CreateException();
        }
    }
}
