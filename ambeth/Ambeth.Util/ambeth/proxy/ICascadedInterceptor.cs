using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
#if !SILVERLIGHT
using Castle.DynamicProxy;
using System.Threading;
#else
using Castle.Core.Interceptor;
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Proxy;

namespace De.Osthus.Ambeth.Proxy
{
    public interface ICascadedInterceptor
        : IInterceptor
    {
        Object Target { get; set; }
    }
}