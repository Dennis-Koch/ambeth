using System;
using System.Collections.Generic;

#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Remote
{
    public interface IRemoteInterceptor : IInterceptor
    {
        String ServiceName { get; set; }
    }
}
