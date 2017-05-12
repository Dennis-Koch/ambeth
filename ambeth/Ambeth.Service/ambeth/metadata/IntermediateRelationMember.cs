using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class IntermediateRelationMember : RelationMember
    {
        protected readonly String propertyName;

        protected readonly Type declaringType;

        protected readonly Type entityType;

        protected readonly Type realType;

        protected readonly Type elementType;

        protected readonly HashMap<Type, Attribute> annotationMap;

        public IntermediateRelationMember(Type declaringType, Type entityType, Type realType, Type elementType, String propertyName, Attribute[] annotations)
        {
            this.declaringType = declaringType;
            this.entityType = entityType;
            this.realType = realType;
            this.elementType = elementType;
            this.propertyName = propertyName;
            if (annotations != null)
		    {
			    annotationMap = new HashMap<Type, Attribute>();
                foreach (Attribute annotation in annotations)
			    {
				    annotationMap.Put(annotation.GetType(), annotation);
			    }
		    }
		    else
		    {
			    annotationMap = null;
		    }
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
                return declaringType;
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
            return annotationMap.Get(annotationType);
        }

        protected Exception CreateException()
        {
            return new NotSupportedException("This in an intermediate member which works only as a stub for a later bytecode-enhanced member");
        }

        public override bool IsManyTo
        {
            get
            {
                throw CreateException();
            }
        }

        public override CascadeLoadMode CascadeLoadMode
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
                return ListUtil.IsCollection(RealType);
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
                return entityType;
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
