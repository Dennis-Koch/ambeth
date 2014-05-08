using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Property, Inherited = true, AllowMultiple = false)]
    public class ParentChildAttribute : Attribute
    {
        // Intended blank
    }
}
