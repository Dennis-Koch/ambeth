using System;
using System.Collections.Generic;
using Castle.DynamicProxy;
using System.Reflection.Emit;
using System.Runtime.Serialization;
using System.Reflection;
using De.Osthus.Ambeth.Proxy;

namespace De.Osthus.Ambeth.Proxy
{
    public class DefaultProxyOptionsFactory : IProxyOptionsFactory
    {
        public virtual ProxyGenerationOptions CreateProxyGenerationOptions()
        {
            return new ProxyGenerationOptions();
        }
    }
}
