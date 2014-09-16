#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using System;
using De.Osthus.Ambeth.Proxy;

namespace De.Osthus.Ambeth.Ioc.Proxy
{
    public class EmptyInterceptor : AbstractSimpleInterceptor
    {
        public static readonly IInterceptor INSTANCE = new EmptyInterceptor();

        private EmptyInterceptor()
        {
            // Intended blank
        }

        protected override void InterceptIntern(IInvocation invocation)
        {
            if (typeof(Object).Equals(invocation.Method.DeclaringType))
            {
                invocation.ReturnValue = invocation.Method.Invoke(this, invocation.Arguments);
                return;
            }
            throw new NotSupportedException("Should never be called");
        }
    }
}
