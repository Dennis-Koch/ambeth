package com.koch.ambeth.cache.proxy;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Set;

import com.koch.ambeth.cache.CacheContext;
import com.koch.ambeth.cache.CacheType;
import com.koch.ambeth.cache.config.CacheNamedBeans;
import com.koch.ambeth.cache.interceptor.CacheContextInterceptor;
import com.koch.ambeth.cache.interceptor.CacheInterceptor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.interceptor.MergeInterceptor;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

public class CacheContextPostProcessor extends AbstractCascadePostProcessor {
	protected AnnotationCache<CacheContext> annotationCache = new AnnotationCache<CacheContext>(
			CacheContext.class) {
		@Override
		protected boolean annotationEquals(CacheContext left, CacheContext right) {
			return Objects.equals(left.value(), right.value());
		}
	};

	@Autowired
	protected CachePostProcessor cachePostProcessor;

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory,
			IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
			Set<Class<?>> requestedTypes) {
		CacheContext cacheContext = annotationCache.getAnnotation(type);
		if (cacheContext == null) {
			return null;
		}
		IMethodLevelBehavior<Annotation> cacheBehavior = cachePostProcessor
				.createInterceptorModeBehavior(type);

		CacheInterceptor interceptor = new CacheInterceptor();
		if (beanContext.isRunning()) {
			interceptor = beanContext.registerWithLifecycle(interceptor)//
					.propertyValue(MergeInterceptor.BEHAVIOR_PROP, cacheBehavior)//
					.ignoreProperties(MergeInterceptor.PROCESS_SERVICE_PROP,
							MergeInterceptor.SERVICE_NAME_PROP)//
					.finish();
		}
		else {
			beanContextFactory.registerWithLifecycle(interceptor)//
					.propertyValue(MergeInterceptor.BEHAVIOR_PROP, cacheBehavior)//
					.ignoreProperties(MergeInterceptor.PROCESS_SERVICE_PROP,
							MergeInterceptor.SERVICE_NAME_PROP);
		}
		CacheType cacheType = cacheContext.value();
		String cacheProviderName;
		switch (cacheType) {
			case PROTOTYPE: {
				cacheProviderName = CacheNamedBeans.CacheProviderPrototype;
				break;
			}
			case SINGLETON: {
				cacheProviderName = CacheNamedBeans.CacheProviderSingleton;
				break;
			}
			case THREAD_LOCAL: {
				cacheProviderName = CacheNamedBeans.CacheProviderThreadLocal;
				break;
			}
			case DEFAULT: {
				return interceptor;
			}
			default:
				throw new IllegalStateException("Not supported type: " + cacheType);
		}
		CacheContextInterceptor ccInterceptor = new CacheContextInterceptor();
		if (beanContext.isRunning()) {
			return beanContext.registerWithLifecycle(ccInterceptor)//
					.propertyRef("CacheProvider", cacheProviderName)//
					.propertyValue("Target", interceptor)//
					.finish();
		}
		beanContextFactory.registerWithLifecycle(ccInterceptor)//
				.propertyRef("CacheProvider", cacheProviderName)//
				.propertyValue("Target", interceptor);
		return ccInterceptor;
	}

	@Override
	protected Annotation lookForAnnotation(AnnotatedElement member) {
		Annotation annotation = super.lookForAnnotation(member);
		if (annotation != null) {
			return annotation;
		}
		return member.getAnnotation(CacheContext.class);
	}
}
