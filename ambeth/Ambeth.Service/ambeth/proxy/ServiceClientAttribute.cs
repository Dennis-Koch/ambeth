using System;

namespace De.Osthus.Ambeth.Proxy
{
    [AttributeUsage(AttributeTargets.Interface)]
    public class ServiceClientAttribute : Attribute
    {
        public String Name { get; set; }
    }
}
