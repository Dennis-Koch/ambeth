#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using System;

namespace De.Osthus.Ambeth.Ioc.Proxy
{
    public class EmptyInterceptor : IInterceptor
    {
        public static readonly IInterceptor INSTANCE = new EmptyInterceptor();

        private EmptyInterceptor()
        {
            // Intended blank
        }

        public void Intercept(IInvocation invocation)
        {
            throw new NotSupportedException("Should never be called");
        }
    }
}
