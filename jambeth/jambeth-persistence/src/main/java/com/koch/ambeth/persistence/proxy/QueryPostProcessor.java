package com.koch.ambeth.persistence.proxy;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

/*-
 * #%L
 * jambeth-persistence
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

import java.util.Set;

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.interceptor.QueryInterceptor;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.service.proxy.IBehaviorTypeExtractor;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.service.proxy.MethodLevelBehavior;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.annotation.SmartQuery;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

public class QueryPostProcessor extends AbstractCascadePostProcessor {
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<SmartQuery> smartQueryAnnotationCache =
			new AnnotationCache<SmartQuery>(SmartQuery.class) {
				@Override
				protected boolean annotationEquals(SmartQuery left, SmartQuery right) {
					return Objects.equals(left.entityType(), right.entityType());
				}
			};

	IBehaviorTypeExtractor<SmartQuery, SmartQuery> btExtractor =
			new IBehaviorTypeExtractor<SmartQuery, SmartQuery>() {
				@Override
				public SmartQuery extractBehaviorType(SmartQuery annotation,
						AnnotatedElement annotatedElement) {
					return annotation;
				}
			};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory,
			IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
			Set<Class<?>> requestedTypes) {
		IProperties props = beanContext.getService(IProperties.class);
		boolean networkClientMode = Boolean
				.parseBoolean(props.getString(ServiceConfigurationConstants.NetworkClientMode, "false"));
		if (networkClientMode) {
			// for the smart query behavior we don't need logic on the RPC stub side
			return null;
		}
		IMethodLevelBehavior<SmartQuery> behaviour =
				MethodLevelBehavior.create(type, smartQueryAnnotationCache, SmartQuery.class, btExtractor,
						beanContextFactory, beanContext);
		if (behaviour == null) {
			return null;
		}
		QueryInterceptor interceptor = new QueryInterceptor();
		if (beanContext.isRunning()) {
			IBeanRuntime<QueryInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor)
					.propertyValue(QueryInterceptor.P_BEHAVIOUR, behaviour);
			return interceptorBC.finish();
		}
		beanContextFactory.registerWithLifecycle(interceptor)//
				.propertyValue(QueryInterceptor.P_BEHAVIOUR, behaviour);
		return interceptor;
	}
}
