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

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

import net.sf.cglib.proxy.MethodProxy;

public class ThreadLocalRootCacheInterceptor extends AbstractRootCacheAwareInterceptor
{
	protected static final Method clearMethod = AbstractRootCacheAwareInterceptor.clearMethod;

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(defaultValue = "false")
	protected boolean privileged;

	@Forkable
	protected final ThreadLocal<RootCache> rootCacheTL = new SensitiveThreadLocal<RootCache>();

	protected IRootCache getCurrentRootCache()
	{
		IRootCache rootCache = getCurrentRootCacheIfValid();
		if (rootCache == null)
		{
			rootCache = acquireRootCache(privileged, rootCacheTL);
		}
		return rootCache;
	}

	protected IRootCache getCurrentRootCacheIfValid()
	{
		return rootCacheTL.get();
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (clearMethod.equals(method))
		{
			IRootCache rootCache = getCurrentRootCacheIfValid();
			if (rootCache == null)
			{
				// Nothing to do
				return null;
			}
		}
		IRootCache rootCache = getCurrentRootCache();
		return proxy.invoke(rootCache, args);
	}

	@Override
	public void cleanupThreadLocal()
	{
		disposeCurrentRootCache(rootCacheTL);
	}
}
