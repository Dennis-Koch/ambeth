using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Method, Inherited = false, AllowMultiple = false)]
    public class MergeAttribute : Attribute
    {
        // Intended blank
    }
}
