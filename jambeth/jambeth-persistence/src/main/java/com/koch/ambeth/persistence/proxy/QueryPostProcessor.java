package com.koch.ambeth.persistence.proxy;

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

import com.koch.ambeth.cache.CacheContext;
import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.interceptor.QueryInterceptor;
import com.koch.ambeth.query.squery.ISquery;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.service.proxy.ServiceClient;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

public class QueryPostProcessor extends AbstractCascadePostProcessor {
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<CacheContext> cacheContextAnnotationCache =
			new AnnotationCache<CacheContext>(CacheContext.class) {
				@Override
				protected boolean annotationEquals(CacheContext left, CacheContext right) {
					return Objects.equals(left.value(), right.value());
				}
			};

	protected final AnnotationCache<Service> serviceAnnotationCache =
			new AnnotationCache<Service>(Service.class) {
				@Override
				protected boolean annotationEquals(Service left, Service right) {
					return Objects.equals(left.value(), right.value());
				}
			};

	protected final AnnotationCache<ServiceClient> serviceClientAnnotationCache =
			new AnnotationCache<ServiceClient>(ServiceClient.class) {
				@Override
				protected boolean annotationEquals(ServiceClient left, ServiceClient right) {
					return Objects.equals(left.value(), right.value());
				}
			};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory,
			IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
			Set<Class<?>> requestedTypes) {
		CacheContext cacheContextAnnotation = cacheContextAnnotationCache.getAnnotation(type);
		Service serviceAnnotation = serviceAnnotationCache.getAnnotation(type);
		ServiceClient serviceClientAnnotation = serviceClientAnnotationCache.getAnnotation(type);
		if (serviceClientAnnotation != null
				|| (cacheContextAnnotation == null && serviceAnnotation == null)) {
			if (!ISquery.class.isAssignableFrom(type)) // this added by shuang, if any regist bean is
																									// ISuery implement, it can be intercepted
			{
				return null;
			}
		}

		QueryInterceptor interceptor = new QueryInterceptor();
		if (beanContext.isRunning()) {
			IBeanRuntime<QueryInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
			return interceptorBC.finish();
		}
		beanContextFactory.registerWithLifecycle(interceptor);
		return interceptor;
	}
}
