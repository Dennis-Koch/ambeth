using System;
using System.Collections.Generic;
using Castle.DynamicProxy;
using System.Reflection.Emit;
using System.Runtime.Serialization;
using System.Reflection;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IProxyOptionsFactory
    {
        ProxyGenerationOptions CreateProxyGenerationOptions();
    }
}
