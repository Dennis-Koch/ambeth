using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Annotation
{
    [AttributeUsage(AttributeTargets.Class, Inherited = false, AllowMultiple = false)]
    public class ConfigurationConstants : Attribute
    {
        // Intended blank
    }
}
