using System;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public class TargetingInterceptor : AbstractSimpleInterceptor
    {
        public static TargetingInterceptor Create(ITargetProvider threadLocalProvider)
        {
            TargetingInterceptor interceptor = new TargetingInterceptor();
            interceptor.TargetProvider = threadLocalProvider;
            return interceptor;
        }

        public ITargetProvider TargetProvider { get; set; }

        protected override void InterceptIntern(IInvocation invocation)
        {
            Object target = TargetProvider.GetTarget();
            if (target == null)
            {
                throw new NullReferenceException("Object reference has to be valid. TargetProvider: " + TargetProvider);
            }
            invocation.ReturnValue = invocation.Method.Invoke(target, invocation.Arguments);
        }
    }
}