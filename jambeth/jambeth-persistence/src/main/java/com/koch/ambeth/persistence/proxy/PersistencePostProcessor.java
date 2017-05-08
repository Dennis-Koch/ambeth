package com.koch.ambeth.persistence.proxy;

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

import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.service.proxy.IBehaviorTypeExtractor;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.service.proxy.MethodLevelBehavior;
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

public class PersistencePostProcessor extends AbstractCascadePostProcessor {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<PersistenceContext> annotationCache =
			new AnnotationCache<PersistenceContext>(PersistenceContext.class) {
				@Override
				protected boolean annotationEquals(PersistenceContext left, PersistenceContext right) {
					return EqualsUtil.equals(left.value(), right.value());
				}
			};

	protected final IBehaviorTypeExtractor<PersistenceContext, PersistenceContextType> btExtractor =
			new IBehaviorTypeExtractor<PersistenceContext, PersistenceContextType>() {
				@Override
				public PersistenceContextType extractBehaviorType(PersistenceContext annotation,
						AnnotatedElement annotatedElement) {
					return annotation.value();
				}
			};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory,
			IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
			Set<Class<?>> requestedTypes) {
		IMethodLevelBehavior<PersistenceContextType> behaviour =
				MethodLevelBehavior.create(type, annotationCache, PersistenceContextType.class, btExtractor,
						beanContextFactory, beanContext);
		if (behaviour == null) {
			return null;
		}
		PersistenceContextInterceptor interceptor = new PersistenceContextInterceptor();
		if (beanContext.isRunning()) {
			IBeanRuntime<PersistenceContextInterceptor> interceptorBC =
					beanContext.registerWithLifecycle(interceptor);
			interceptorBC.propertyValue("MethodLevelBehaviour", behaviour);
			return interceptorBC.finish();
		}
		IBeanConfiguration interceptorBC = beanContextFactory.registerWithLifecycle(interceptor);
		interceptorBC.propertyValue("MethodLevelBehaviour", behaviour);
		return interceptor;
	}
}
