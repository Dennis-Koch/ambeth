package com.koch.ambeth.cache.interceptor;

/*-
 * #%L
 * jambeth-cache
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Stack;

import com.koch.ambeth.cache.ICacheProviderExtendable;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.ForkableType;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

import net.sf.cglib.proxy.MethodProxy;

public class CacheProviderInterceptor extends AbstractSimpleInterceptor
		implements ICacheProviderExtendable, ICacheProvider, ICacheContext, IThreadLocalCleanupBean {
	private static final Set<Method> methodsDirectlyToRootCache = new HashSet<>();

	private static final Method currentCacheMethod;

	static {
		try {
			methodsDirectlyToRootCache.add(ICache.class.getMethod("getReadLock"));
			methodsDirectlyToRootCache.add(ICache.class.getMethod("getWriteLock"));
			currentCacheMethod = ICache.class.getMethod("getCurrentCache");
		}
		catch (NoSuchMethodException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Stack<ICacheProvider> cacheProviderStack = new Stack<>();

	@Forkable(ForkableType.SHALLOW_COPY)
	protected final ThreadLocal<ArrayList<ICacheProvider>> cacheProviderStackTL = new SensitiveThreadLocal<>();

	@Autowired
	protected ICacheProvider threadLocalCacheProvider;

	@Autowired
	protected IRootCache rootCache;

	@Override
	public void cleanupThreadLocal() {
		cacheProviderStackTL.remove();
	}

	@Override
	public void registerCacheProvider(ICacheProvider cacheProvider) {
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");
		cacheProviderStack.push(cacheProvider);
	}

	@Override
	public void unregisterCacheProvider(ICacheProvider cacheProvider) {
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");
		if (cacheProviderStack.peek() != cacheProvider) {
			throw new IllegalStateException(
					"The current cacheProvider is not the one specified to unregister");
		}
		cacheProviderStack.pop();
	}

	public ICacheProvider getCurrentCacheProvider() {
		ArrayList<ICacheProvider> stack = cacheProviderStackTL.get();
		if (stack != null && !stack.isEmpty()) {
			return stack.peek();
		}
		if (!cacheProviderStack.isEmpty()) {
			return cacheProviderStack.peek();
		}
		return null;
	}

	@Override
	public ICache getCurrentCache() {
		return getCurrentCacheProvider().getCurrentCache();
	}

	@Override
	public boolean isNewInstanceOnCall() {
		return getCurrentCacheProvider().isNewInstanceOnCall();
	}

	@Override
	public IStateRollback pushCache(ICache cache, IStateRollback... rollbacks) {
		return pushCache(new SingleCacheProvider(cache));
	}

	@Override
	public IStateRollback pushCache(final ICacheProvider cacheProvider, IStateRollback... rollbacks) {
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");

		ArrayList<ICacheProvider> stack = cacheProviderStackTL.get();
		if (stack == null) {
			stack = new ArrayList<>();
			cacheProviderStackTL.set(stack);
		}
		stack.add(cacheProvider);

		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Exception {
				ArrayList<ICacheProvider> stack = cacheProviderStackTL.get();
				if (stack.popLastElement() != cacheProvider) {
					throw new IllegalStateException("Must never happen");
				}
			}
		};
	}

	@Override
	public <R> R executeWithCache(IResultingBackgroundWorkerDelegate<R> runnable) throws Exception {
		return executeWithCache(threadLocalCacheProvider, runnable);
	}

	@Override
	public <R, T> R executeWithCache(IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state)
			throws Exception {
		return executeWithCache(threadLocalCacheProvider, runnable, state);
	}

	@Override
	public <R> R executeWithCache(ICacheProvider cacheProvider,
			IResultingBackgroundWorkerDelegate<R> runnable) throws Exception {
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");
		ParamChecker.assertParamNotNull(runnable, "runnable");

		ArrayList<ICacheProvider> stack = cacheProviderStackTL.get();
		if (stack == null) {
			stack = new ArrayList<>();
			cacheProviderStackTL.set(stack);
		}
		stack.add(cacheProvider);
		try {
			return runnable.invoke();
		}
		finally {
			if (stack.popLastElement() != cacheProvider) {
				throw new IllegalStateException("Must never happen");
			}
		}
	}

	@Override
	public <R, T> R executeWithCache(ICacheProvider cacheProvider,
			IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state) throws Exception {
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");
		ParamChecker.assertParamNotNull(runnable, "runnable");

		ArrayList<ICacheProvider> stack = cacheProviderStackTL.get();
		if (stack == null) {
			stack = new ArrayList<>();
			cacheProviderStackTL.set(stack);
		}
		stack.add(cacheProvider);
		try {
			return runnable.invoke(state);
		}
		finally {
			if (stack.popLastElement() != cacheProvider) {
				throw new IllegalStateException("Must never happen");
			}
		}
	}

	@Override
	public <R> R executeWithCache(ICache cache, IResultingBackgroundWorkerDelegate<R> runnable)
			throws Exception {
		ParamChecker.assertParamNotNull(cache, "cache");
		ParamChecker.assertParamNotNull(runnable, "runnable");
		return executeWithCache(new SingleCacheProvider(cache), runnable);
	}

	@Override
	public <R, T> R executeWithCache(ICache cache,
			IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state) throws Exception {
		ParamChecker.assertParamNotNull(cache, "cache");
		ParamChecker.assertParamNotNull(runnable, "runnable");
		return executeWithCache(new SingleCacheProvider(cache), runnable, state);
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		ICacheProvider cacheProvider = getCurrentCacheProvider();
		if (cacheProvider == null && currentCacheMethod.equals(method)) {
			return null;
		}
		if (method.getDeclaringClass().equals(ICacheProvider.class)) {
			return proxy.invoke(cacheProvider, args);
		}
		Object target;
		if (!cacheProvider.isNewInstanceOnCall() || !methodsDirectlyToRootCache.contains(method)) {
			target = cacheProvider.getCurrentCache();
		}
		else {
			target = rootCache;
		}
		return proxy.invoke(target, args);
	}
}
