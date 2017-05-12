using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Method, Inherited = false, AllowMultiple = false)]
    public class RemoveAttribute : Attribute
    {
        public Type EntityType { get; protected set; }

	    public String IdName { get; protected set; }

        public RemoveAttribute()
        {
            // Intended blank
        }

        public RemoveAttribute(Type entityType, String idName)
        {
            this.EntityType = entityType;
            this.IdName = idName;
        }
    }
}
