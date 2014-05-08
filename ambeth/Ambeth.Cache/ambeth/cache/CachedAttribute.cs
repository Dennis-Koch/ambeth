using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ServiceModel.Description;

namespace De.Osthus.Ambeth.Cache
{
    [AttributeUsage(AttributeTargets.Method)]
    public class CachedAttribute : Attribute
    {
        public Type Type { get; set; }

        public String AlternateIdName { get; set; }
    }
}
