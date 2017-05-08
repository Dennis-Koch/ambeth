package com.koch.ambeth.service.log.interceptor;

/*-
 * #%L
 * jambeth-service
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
import java.util.Collection;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.LogTypesUtil;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.proxy.CascadedInterceptor;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

import net.sf.cglib.proxy.MethodProxy;

public class LogInterceptor extends CascadedInterceptor {
	public static class IntContainer {
		public int stackLevel;
	}

	private static final ThreadLocal<IntContainer> stackValueTL =
			new SensitiveThreadLocal<IntContainer>() {
				@Override
				protected IntContainer initialValue() {
					return new IntContainer();
				};
			};

	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ILoggerCache loggerCache;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IProperties properties;

	@Property(name = ServiceConfigurationConstants.LogShortNames, defaultValue = "false")
	protected boolean printShortStringNames;

	@Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean isClientLogger;

	@Override
	@SuppressWarnings("rawtypes")
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		Class<?> declaringClass = method.getDeclaringClass();
		if (Object.class.equals(declaringClass)) {
			return invokeTarget(obj, method, args, proxy);
		}
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		IntContainer stackValueContainer = stackValueTL.get();
		stackValueContainer.stackLevel++;
		try {
			long startTicks = 0;
			ILogger loggerOfMethod = loggerCache.getCachedLogger(properties, declaringClass);
			boolean debugEnabled = log.isDebugEnabled() && loggerOfMethod.isDebugEnabled();
			if (debugEnabled) {
				if (!isClientLogger) {
					sb.append("Start:     ");
				}
				else {
					sb.append("Start(S):  ");
				}
				int level = stackValueContainer.stackLevel;
				while (level-- > 1) {
					sb.append(".");
				}
				LogTypesUtil.printMethod(method, printShortStringNames, sb);
				loggerOfMethod.debug(sb.toString());
				sb.setLength(0);

				startTicks = System.currentTimeMillis();
			}
			Object returnValue = invokeTarget(obj, method, args, proxy);
			if (debugEnabled) {
				long endTicks = System.currentTimeMillis();

				int resultCount = returnValue instanceof Collection ? ((Collection) returnValue).size()
						: returnValue != null ? 1 : -1;
				String resultString = resultCount >= 0 ? "" + resultCount : "no";
				String itemsString = void.class.equals(method.getReturnType()) ? ""
						: " with " + resultString + (resultCount != 1 ? " items" : " item");

				if (isClientLogger) {
					sb.append("Finish:    ");
				}
				else {
					sb.append("Finish(S): ");
				}
				int level = stackValueContainer.stackLevel;
				while (level-- > 1) {
					sb.append(".");
				}
				LogTypesUtil.printMethod(method, printShortStringNames, sb);
				sb.append(itemsString).append(" (").append(endTicks - startTicks).append(" ms)");
				loggerOfMethod.debug(sb.toString());
			}
			return returnValue;
		}
		catch (Throwable e) {
			if (beanContext.isRunning()) {
				if (log.isErrorEnabled()) {
					log.error(e);
				}
			}
			throw e;
		}
		finally {
			stackValueContainer.stackLevel--;
			current.dispose(sb);
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + getTarget();
	}
}
