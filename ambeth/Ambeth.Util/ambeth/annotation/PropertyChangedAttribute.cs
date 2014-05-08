using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Property, Inherited = true, AllowMultiple = true)]
    public class PropertyChangedAttribute : Attribute
    {
        public String PropertyName { get; private set; }

        public PropertyChangedAttribute(String propertyName)
        {
            this.PropertyName = propertyName;
        }
    }
}
