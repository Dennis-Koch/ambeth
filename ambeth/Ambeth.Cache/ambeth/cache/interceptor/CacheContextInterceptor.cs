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

namespace De.Osthus.Ambeth.Cache.Interceptor
{

    public class CacheContextInterceptor : CascadedInterceptor, IInitializingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public virtual ICacheContext CacheContext { protected get; set; }

        public virtual ICacheFactory CacheFactory { protected get; set; }

        public virtual ICacheProvider CacheProvider { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(CacheContext, "CacheContext");
            ParamChecker.AssertNotNull(CacheFactory, "CacheFactory");
            ParamChecker.AssertNotNull(CacheProvider, "CacheProvider");
        }

        public override void Intercept(IInvocation invocation)
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
