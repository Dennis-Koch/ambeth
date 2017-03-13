package com.koch.ambeth.persistence.proxy;

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
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;
 
public class QueryPostProcessor extends AbstractCascadePostProcessor
{
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<CacheContext> cacheContextAnnotationCache = new AnnotationCache<CacheContext>(CacheContext.class)
	{
		@Override
		protected boolean annotationEquals(CacheContext left, CacheContext right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	protected final AnnotationCache<Service> serviceAnnotationCache = new AnnotationCache<Service>(Service.class)
	{
		@Override
		protected boolean annotationEquals(Service left, Service right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	protected final AnnotationCache<ServiceClient> serviceClientAnnotationCache = new AnnotationCache<ServiceClient>(ServiceClient.class)
	{
		@Override
		protected boolean annotationEquals(ServiceClient left, ServiceClient right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		CacheContext cacheContextAnnotation = cacheContextAnnotationCache.getAnnotation(type);
		Service serviceAnnotation = serviceAnnotationCache.getAnnotation(type);
		ServiceClient serviceClientAnnotation = serviceClientAnnotationCache.getAnnotation(type);
		if (serviceClientAnnotation != null || (cacheContextAnnotation == null && serviceAnnotation == null))
		{
			if (!ISquery.class.isAssignableFrom(type)) // this added by shuang, if any regist bean is ISuery implement, it can be intercepted
			{
				return null;
			}
		}

		QueryInterceptor interceptor = new QueryInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<QueryInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
			return interceptorBC.finish();
		}
		beanContextFactory.registerWithLifecycle(interceptor);
		return interceptor;
	}
}
