using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Typeinfo
{
    public abstract class TypeInfoItem : ITypeInfoItem
    {
        public static void SetEntityType(Type entityType, ITypeInfoItem member, IProperties properties)
        {
            if (member is TypeInfoItem)
            {
                ((TypeInfoItem)member).ElementType = entityType;
            }
            else
            {
                throw new Exception("TypeInfoItem not supported: " + member);
            }
        }

        public abstract bool IsXMLIgnore { get; }

	    public virtual bool CanRead
	    {
            get
            {
		        return true;
            }
	    }

	    public virtual bool CanWrite
	    {
            get
            {
                return true;
            }
	    }

        public String XMLName { get; set; }

        public Type ElementType { get; set; }

        public abstract Type RealType { get; }

        public Type DeclaringType { get; set; }

        public bool TechnicalMember { get; set; }

        public abstract V GetAnnotation<V>() where V : Attribute;

        public abstract String Name { get; }
        
        public Object DefaultValue { get; set; }

        public Object NullEquivalentValue { get; set; }

        public abstract void SetValue(Object obj, Object value);

        public abstract Object GetValue(Object obj);

        public abstract Object GetValue(Object obj, bool allowNullEquivalentValue);
    }
}