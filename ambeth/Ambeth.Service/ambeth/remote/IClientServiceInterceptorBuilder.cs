using System;
using System.Collections.Generic;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Remote
{
    public interface IClientServiceInterceptorBuilder
    {
        IInterceptor CreateInterceptor(IServiceContext sourceBeanContext, Type syncLocalInterface, Type syncRemoteInterface, Type asyncRemoteInterface);
    }
}
