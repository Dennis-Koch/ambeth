using System;
using System.Collections.Generic;

#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using De.Osthus.Ambeth.Proxy;

namespace De.Osthus.Ambeth.Remote
{
    public interface IRemoteTargetProvider : ITargetProvider
    {
        String ServiceName { get; set; }
    }
}
