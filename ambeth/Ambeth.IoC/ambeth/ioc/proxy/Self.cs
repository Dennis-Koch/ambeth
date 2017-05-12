using System;

namespace De.Osthus.Ambeth.Ioc.Proxy
{
    [AttributeUsage(AttributeTargets.Method | AttributeTargets.Field | AttributeTargets.Property, Inherited = false, AllowMultiple = false)]
    public class Self : Attribute
    {
        // Intended blank
    }
}
