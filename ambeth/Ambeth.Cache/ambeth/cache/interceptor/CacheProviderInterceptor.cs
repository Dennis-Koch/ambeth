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
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class CacheProviderInterceptor : AbstractSimpleInterceptor, ICacheProviderExtendable, ICacheProvider, ICacheContext,
            IThreadLocalCleanupBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        private static readonly ISet<MethodInfo> methodsDirectlyToRootCache = new HashSet<MethodInfo>();

		private static readonly MethodInfo currentCacheMethod;

        static CacheProviderInterceptor()
        {
            methodsDirectlyToRootCache.Add(typeof(ICache).GetProperty("ReadLock").GetGetMethod());
            methodsDirectlyToRootCache.Add(typeof(ICache).GetProperty("WriteLock").GetGetMethod());
			currentCacheMethod = typeof(ICache).GetProperty("CurrentCache").GetGetMethod();
        }

        protected readonly Stack<ICacheProvider> cacheProviderStack = new Stack<ICacheProvider>();

        [Forkable(ForkableType.SHALLOW_COPY)]
        protected readonly ThreadLocal<Stack<ICacheProvider>> cacheProviderStackTL = new ThreadLocal<Stack<ICacheProvider>>();

        [Autowired]
        public ICacheProvider ThreadLocalCacheProvider { protected get; set; }

        [Autowired]
        public IRootCache RootCache { protected get; set; }

        public void CleanupThreadLocal()
        {
            cacheProviderStackTL.Value = null;
        }

        public void RegisterCacheProvider(ICacheProvider cacheProvider)
        {
            ParamChecker.AssertParamNotNull(cacheProvider, "cacheProvider");
            cacheProviderStack.Push(cacheProvider);
        }

        public void UnregisterCacheProvider(ICacheProvider cacheProvider)
        {
            ParamChecker.AssertParamNotNull(cacheProvider, "cacheProvider");
            if (cacheProviderStack.Peek() != cacheProvider)
            {
                throw new Exception("The current cacheProvider is not the one specified to unregister");
            }
            cacheProviderStack.Pop();
        }

        public ICacheProvider GetCurrentCacheProvider()
        {
            Stack<ICacheProvider> stack = cacheProviderStackTL.Value;
            if (stack != null && stack.Count > 0)
            {
                return stack.Peek();
            }
			if (cacheProviderStack.Count > 0)
			{
				return cacheProviderStack.Peek();
			}
			return null;
        }

        public ICache GetCurrentCache()
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

        public R ExecuteWithCache<R>(IResultingBackgroundWorkerDelegate<R> runnable)
        {
            return ExecuteWithCache(ThreadLocalCacheProvider, runnable);
        }

       	public R ExecuteWithCache<R, T>(IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state)
	    {
            return ExecuteWithCache(ThreadLocalCacheProvider, runnable, state);
	    }

        public R ExecuteWithCache<R>(ICacheProvider cacheProvider, IResultingBackgroundWorkerDelegate<R> runnable)
        {
            ParamChecker.AssertParamNotNull(cacheProvider, "cacheProvider");
            ParamChecker.AssertParamNotNull(runnable, "runnable");
            Stack<ICacheProvider> stack = cacheProviderStackTL.Value;
            if (stack == null)
            {
                stack = new Stack<ICacheProvider>();
                cacheProviderStackTL.Value = stack;
            }
            stack.Push(cacheProvider);
            try
            {
                return runnable();
            }
            finally
            {
                if (!Object.ReferenceEquals(stack.Pop(), cacheProvider))
                {
                    throw new Exception("Must never happen");
                }
            }
        }

        public R ExecuteWithCache<R, T>(ICacheProvider cacheProvider, IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state)
	    {
		    ParamChecker.AssertParamNotNull(cacheProvider, "cacheProvider");
		    ParamChecker.AssertParamNotNull(runnable, "runnable");
            Stack<ICacheProvider> stack = cacheProviderStackTL.Value;
            if (stack == null)
            {
                stack = new Stack<ICacheProvider>();
                cacheProviderStackTL.Value = stack;
            }
            stack.Push(cacheProvider);
            try
            {
                return runnable(state);
            }
            finally
            {
                if (!Object.ReferenceEquals(stack.Pop(), cacheProvider))
                {
                    throw new Exception("Must never happen");
                }
            }
	    }

        public R ExecuteWithCache<R>(ICache cache, IResultingBackgroundWorkerDelegate<R> runnable)
        {
            ParamChecker.AssertParamNotNull(cache, "cache");
            ParamChecker.AssertParamNotNull(runnable, "runnable");
            return ExecuteWithCache<R>(new SingleCacheProvider(cache), runnable);
        }

        public R ExecuteWithCache<R, T>(ICache cache, IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state)
	    {
		    ParamChecker.AssertParamNotNull(cache, "cache");
		    ParamChecker.AssertParamNotNull(runnable, "runnable");
            return ExecuteWithCache<R, T>(new SingleCacheProvider(cache), runnable, state);
	    }

        protected override void InterceptIntern(IInvocation invocation)
        {
            ICacheProvider cacheProvider = GetCurrentCacheProvider();
			MethodInfo method = invocation.Method;
			if (cacheProvider == null && currentCacheMethod.Equals(method))
			{
				invocation.ReturnValue = null;
				return;
			}
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