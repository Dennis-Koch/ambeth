using System;
using System.Reflection;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
#if !SILVERLIGHT
using Castle.DynamicProxy;
using System.Threading;
#else
using Castle.Core.Interceptor;
using Castle.DynamicProxy;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public abstract class AbstractSimpleInterceptor : IInterceptor
    {
        public static readonly MethodInfo equalsMethod = ReflectUtil.GetDeclaredMethod(false, typeof(Object), typeof(bool), "Equals", typeof(Object));

        public void Intercept(IInvocation invocation)
        {
            if (equalsMethod.Equals(invocation.Method) && Object.ReferenceEquals(invocation.Arguments[0], invocation.Proxy))
            {
                // Do nothing. This is to prevent unnecessary exceptions in tomcat in REDEPLOY scenarios
                invocation.ReturnValue = true;
                return;
            }
            InterceptIntern(invocation);
        }

        protected abstract void InterceptIntern(IInvocation invocation);
    }
}