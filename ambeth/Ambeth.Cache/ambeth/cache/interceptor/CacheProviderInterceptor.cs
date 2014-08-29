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
using System.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class CacheProviderInterceptor : AbstractSimpleInterceptor, IInitializingBean, ICacheProviderExtendable, ICacheProvider, ICacheContext,
            IThreadLocalCleanupBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public class SingleCacheProvider : ICacheProvider
        {
            protected ICache cache;

            public SingleCacheProvider(ICache cache)
            {
                this.cache = cache;
            }

            public ICache GetCurrentCache()
            {
                return cache;
            }

            public bool IsNewInstanceOnCall
            {
                get
                {
                    return false;
                }
            }
        }

        private static readonly ISet<MethodInfo> methodsDirectlyToRootCache = new HashSet<MethodInfo>();

        static CacheProviderInterceptor()
        {
            methodsDirectlyToRootCache.Add(typeof(ICache).GetProperty("ReadLock").GetGetMethod());
            methodsDirectlyToRootCache.Add(typeof(ICache).GetProperty("WriteLock").GetGetMethod());
        }

        protected readonly Stack<ICacheProvider> cacheProviderStack = new Stack<ICacheProvider>();

        protected readonly ThreadLocal<Stack<ICacheProvider>> cacheProviderStackTL = new ThreadLocal<Stack<ICacheProvider>>();

        public virtual ICacheProvider ThreadLocalCacheProvider { protected get; set; }

        public virtual IRootCache RootCache { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(RootCache, "RootCache");
            ParamChecker.AssertNotNull(ThreadLocalCacheProvider, "ThreadLocalCacheProvider");
        }

        public virtual void CleanupThreadLocal()
        {
            cacheProviderStackTL.Value = null;
        }

        public virtual void RegisterCacheProvider(ICacheProvider cacheProvider)
        {
            ParamChecker.AssertParamNotNull(cacheProvider, "cacheProvider");
            cacheProviderStack.Push(cacheProvider);
        }

        public virtual void UnregisterCacheProvider(ICacheProvider cacheProvider)
        {
            ParamChecker.AssertParamNotNull(cacheProvider, "cacheProvider");
            if (cacheProviderStack.Peek() != cacheProvider)
            {
                throw new Exception("The current cacheProvider is not the one specified to unregister");
            }
            cacheProviderStack.Pop();
        }

        public virtual ICacheProvider GetCurrentCacheProvider()
        {
            Stack<ICacheProvider> stack = cacheProviderStackTL.Value;
            if (stack != null && stack.Count > 0)
            {
                return stack.Peek();
            }
            return cacheProviderStack.Peek();
        }

        public virtual ICache GetCurrentCache()
        {
            return GetCurrentCacheProvider().GetCurrentCache();
        }

        public bool IsNewInstanceOnCall
        {
            get
            {
                return GetCurrentCacheProvider().IsNewInstanceOnCall;
            }
        }

        public R ExecuteWithCache<R>(ISingleCacheRunnable<R> runnable)
        {
            return ExecuteWithCache(ThreadLocalCacheProvider, runnable);
        }

       	public R ExecuteWithCache<R, T>(ISingleCacheParamRunnable<R, T> runnable, T state)
	    {
            return ExecuteWithCache(ThreadLocalCacheProvider, runnable, state);
	    }

        public R ExecuteWithCache<R>(ICacheProvider cacheProvider, ISingleCacheRunnable<R> runnable)
        {
            ParamChecker.AssertParamNotNull(cacheProvider, "cacheProvider");
            ParamChecker.AssertParamNotNull(runnable, "runnable");
            ICache cache = cacheProvider.GetCurrentCache();
            return ExecuteWithCache(cache, runnable);
        }

        public R ExecuteWithCache<R, T>(ICacheProvider cacheProvider, ISingleCacheParamRunnable<R, T> runnable, T state)
	    {
		    ParamChecker.AssertParamNotNull(cacheProvider, "cacheProvider");
		    ParamChecker.AssertParamNotNull(runnable, "runnable");
		    ICache cache = cacheProvider.GetCurrentCache();
		    return ExecuteWithCache(cache, runnable, state);
	    }

        public R ExecuteWithCache<R>(ICache cache, ISingleCacheRunnable<R> runnable)
        {
            ParamChecker.AssertParamNotNull(cache, "cache");
            ParamChecker.AssertParamNotNull(runnable, "runnable");
            ICacheProvider singletonCacheProvider = new SingleCacheProvider(cache);
            Stack<ICacheProvider> stack = cacheProviderStackTL.Value;
            if (stack == null)
            {
                stack = new Stack<ICacheProvider>();
                cacheProviderStackTL.Value = stack;
            }
            stack.Push(singletonCacheProvider);
            try
            {
                return runnable.Invoke();
            }
            finally
            {
                if (stack.Pop() != singletonCacheProvider)
                {
                    throw new Exception("Must never happen");
                }
            }
        }

        public R ExecuteWithCache<R, T>(ICache cache, ISingleCacheParamRunnable<R, T> runnable, T state)
	    {
		    ParamChecker.AssertParamNotNull(cache, "cache");
		    ParamChecker.AssertParamNotNull(runnable, "runnable");
		    ICacheProvider singletonCacheProvider = new SingleCacheProvider(cache);

		    Stack<ICacheProvider> stack = cacheProviderStackTL.Value;
		    if (stack == null)
		    {
			    stack = new Stack<ICacheProvider>();
			    cacheProviderStackTL.Value = stack;
		    }
		    stack.Push(singletonCacheProvider);
		    try
		    {
			    return runnable.Invoke(state);
		    }
		    finally
		    {
			    if (stack.Pop() != singletonCacheProvider)
			    {
                    throw new Exception("Must never happen");
			    }
		    }
	    }

        protected override void InterceptIntern(IInvocation invocation)
        {
            ICacheProvider cacheProvider = GetCurrentCacheProvider();
            MethodInfo method = invocation.Method;
            if (method.DeclaringType.Equals(typeof(ICacheProvider)))
		    {
                invocation.ReturnValue = method.Invoke(cacheProvider, invocation.Arguments);
                return;
		    }
            Object target;
            if (!cacheProvider.IsNewInstanceOnCall || !methodsDirectlyToRootCache.Contains(invocation.Method))
            {
                target = cacheProvider.GetCurrentCache();
            }
            else
            {
                target = RootCache;
            }
            invocation.ReturnValue = method.Invoke(target, invocation.Arguments);
        }
    }
}