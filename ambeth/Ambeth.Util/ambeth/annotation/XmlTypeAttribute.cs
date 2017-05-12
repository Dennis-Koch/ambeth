using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Interface | AttributeTargets.Enum, Inherited = false, AllowMultiple = false)]
    public class XmlTypeAttribute : Attribute
    {
        public String Name { get; set; }

        public String Namespace { get; set; }

        public XmlTypeAttribute()
        {
            // Intended blank
        }
    }
}
