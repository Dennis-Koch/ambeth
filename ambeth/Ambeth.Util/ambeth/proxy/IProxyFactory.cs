using System;
using System.Collections.Generic;
using System.Reflection.Emit;
using System.Runtime.Serialization;
using System.Reflection;
using Castle.DynamicProxy;
#if !SILVERLIGHT
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public interface IProxyFactory
    {
        T CreateProxy<T>(params IInterceptor[] interceptors);

        T CreateProxy<T>(Type[] interfaces, params IInterceptor[] interceptors);

        Object CreateProxy(Type type, params IInterceptor[] interceptors);

        Object CreateProxy(Type type, Type[] interfaces, params IInterceptor[] interceptors);

        Object CreateProxy(Type[] interfaces, params IInterceptor[] interceptors);

        ICascadedInterceptor Wrap(Object target, ICascadedInterceptor interceptor);
    }
}
