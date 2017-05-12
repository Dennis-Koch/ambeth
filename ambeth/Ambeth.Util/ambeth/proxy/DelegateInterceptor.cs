using System;
using System.Reflection;
using Castle.DynamicProxy;
using De.Osthus.Ambeth.Collections;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public class DelegateInterceptor : AbstractSimpleInterceptor
    {
        protected readonly Object target;

        protected readonly HashMap<MethodInfo, MethodInfo> methodMap;

        public DelegateInterceptor(Object target, HashMap<MethodInfo, MethodInfo> methodMap)
        {
            this.target = target;
            this.methodMap = methodMap;
        }

        protected override void InterceptIntern(IInvocation invocation)
        {
            MethodInfo mappedMethod = methodMap.Get(invocation.Method);
            if (mappedMethod == null)
            {
                invocation.ReturnValue = invocation.Method.Invoke(target, invocation.Arguments);
                return;
            }
            invocation.ReturnValue = mappedMethod.Invoke(target, invocation.Arguments);
        }
    }
}
