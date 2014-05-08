using System;

namespace De.Osthus.Ambeth.Proxy
{
    [AttributeUsage(AttributeTargets.Class)]
    public class ProxyAttribute : Attribute
    {
        public String Type { get; private set; }
    }
}
