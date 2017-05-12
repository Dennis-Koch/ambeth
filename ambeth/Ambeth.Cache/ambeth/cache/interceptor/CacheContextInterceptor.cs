using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Ioc;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using System.Reflection;
using System;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class CacheContextInterceptor : CascadedInterceptor
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        [Autowired]
        public ICacheContext CacheContext { protected get; set; }

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        [Autowired]
        public ICacheProvider CacheProvider { protected get; set; }

        protected override void InterceptIntern(IInvocation invocation)
        {
            MethodInfo method = invocation.Method;
            if (method.DeclaringType.Equals(typeof(Object)))
            {
                InvokeTarget(invocation);
                return;
            }
            Object result = CacheContext.ExecuteWithCache(CacheProvider, delegate()
            {
                InvokeTarget(invocation);
                return invocation.ReturnValue;
            });
        }
    }
}
