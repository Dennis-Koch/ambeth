package com.koch.ambeth.security.server.cache;

/*-
 * #%L
 * jambeth-security-server
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
import java.lang.reflect.Modifier;
import java.util.Set;

import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IOrderedBeanProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.typeinfo.TypeInfoItemUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.security.server.SecurityFilterInterceptor;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.service.proxy.MethodLevelBehavior;
import com.koch.ambeth.service.proxy.MethodLevelHashMap;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

public class CommittedRootCachePostProcessor extends AbstractCascadePostProcessor
		implements IOrderedBeanProcessor {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory,
			IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
			Set<Class<?>> requestedTypes) {
		if (!CacheModule.COMMITTED_ROOT_CACHE.equals(beanConfiguration.getName())) {
			return null;
		}
		MethodLevelHashMap<SecurityContextType> methodLevelBehaviour = null;

		Method[] methods = ReflectUtil.getMethods(type);
		for (int a = methods.length; a-- > 0;) {
			Method method = methods[a];

			if ((method.getModifiers() & Modifier.PUBLIC) == 0
					|| void.class.equals(method.getReturnType())) {
				// ignore this method
				continue;
			}
			Class<?> elementReturnType = TypeInfoItemUtil
					.getElementTypeUsingReflection(method.getReturnType(), method.getGenericReturnType());
			if (!IObjRef.class.isAssignableFrom(elementReturnType)
					&& !IObjRelationResult.class.isAssignableFrom(elementReturnType)
					&& !Object.class.equals(elementReturnType)) {
				// ignore this method
				continue;
			}
			if (method.getName().equals("createCacheValueInstance")) {
				// ignore this method
				continue;
			}
			if (methodLevelBehaviour == null) {
				methodLevelBehaviour = new MethodLevelHashMap<SecurityContextType>();
			}
			methodLevelBehaviour.put(method.getName(), method.getParameterTypes(),
					SecurityContextType.AUTHENTICATED);
		}
		if (methodLevelBehaviour == null) {
			methodLevelBehaviour = new MethodLevelHashMap<SecurityContextType>(0);
		}
		IMethodLevelBehavior<SecurityContextType> behaviour =
				new MethodLevelBehavior<SecurityContextType>(SecurityContextType.NOT_REQUIRED,
						methodLevelBehaviour);

		SecurityFilterInterceptor interceptor = new SecurityFilterInterceptor();
		if (beanContext.isRunning()) {
			IBeanRuntime<SecurityFilterInterceptor> interceptorBC =
					beanContext.registerWithLifecycle(interceptor);
			interceptorBC.propertyValue("MethodLevelBehaviour", behaviour)
					.propertyValue(SecurityFilterInterceptor.PROP_CHECK_METHOD_ACCESS, Boolean.FALSE);
			return interceptorBC.finish();
		}
		IBeanConfiguration interceptorBC = beanContextFactory.registerWithLifecycle(interceptor);
		interceptorBC.propertyValue("MethodLevelBehaviour", behaviour)
				.propertyValue(SecurityFilterInterceptor.PROP_CHECK_METHOD_ACCESS, Boolean.FALSE);
		return interceptor;

	}

	@Override
	public ProcessorOrder getOrder() {
		// A security filter interceptor has to be one of the OUTERMOST proxies of all potential
		// postprocessor-created proxies
		// The proxy order is the inverse order of their creating postprocessors
		return ProcessorOrder.LOW;
	}
}
