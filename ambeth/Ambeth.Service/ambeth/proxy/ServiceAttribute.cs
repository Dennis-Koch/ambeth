using System;

namespace De.Osthus.Ambeth.Proxy
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Interface)]
    public class ServiceAttribute : Attribute
    {
        public String Name { get; set; }

        public Type Interface { get; set; }

        public bool CustomExport { get; set; }
    }
}
