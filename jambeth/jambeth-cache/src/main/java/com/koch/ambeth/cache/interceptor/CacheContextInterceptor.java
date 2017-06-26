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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.CascadedInterceptor;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

import net.sf.cglib.proxy.MethodProxy;

public class CacheContextInterceptor extends CascadedInterceptor {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ICacheProvider cacheProvider;

	@Override
	protected Object interceptIntern(final Object obj, final Method method, final Object[] args,
			final MethodProxy proxy) throws Throwable {
		if (method.getDeclaringClass().equals(Object.class)) {
			return invokeTarget(obj, method, args, proxy);
		}
		try {
			return cacheContext.executeWithCache(cacheProvider,
					new IResultingBackgroundWorkerDelegate<Object>() {
						@Override
						public Object invoke() throws Exception {
							try {
								return invokeTarget(obj, method, args, proxy);
							}
							catch (Error e) {
								throw e;
							}
							catch (Throwable e) {
								throw RuntimeExceptionUtil.mask(e);
							}
						}
					});
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
		}
	}
}
