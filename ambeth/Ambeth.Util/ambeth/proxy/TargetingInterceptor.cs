using System;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public class TargetingInterceptor : IInterceptor
    {
        public static TargetingInterceptor Create(ITargetProvider threadLocalProvider)
        {
            TargetingInterceptor interceptor = new TargetingInterceptor();
            interceptor.TargetProvider = threadLocalProvider;
            return interceptor;
        }

        public ITargetProvider TargetProvider { get; set; }

        public void Intercept(IInvocation invocation)
        {
            Object target = TargetProvider.GetTarget();
            invocation.ReturnValue = invocation.Method.Invoke(target, invocation.Arguments);
        }
    }
}