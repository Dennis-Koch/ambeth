using System;

namespace De.Osthus.Ambeth.Proxy
{
    [AttributeUsage(AttributeTargets.Interface)]
    public class ClientServiceAttribute : Attribute
    {
        public String Name { get; set; }

        public Type WCFInterface { get; set; }
    }
}
