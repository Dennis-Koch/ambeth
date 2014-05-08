using System;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Method, Inherited = true, AllowMultiple = false)]
    public class PropertyAccessor : Attribute
    {
        public String PropertyName { get; private set; }

        public PropertyAccessor(String propertyName)
        {
            this.PropertyName = propertyName;
        }
    }
}
