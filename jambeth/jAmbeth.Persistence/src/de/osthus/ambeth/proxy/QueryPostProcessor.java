package de.osthus.ambeth.proxy;

import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.cache.CacheContext;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.interceptor.QueryInterceptor;
import de.osthus.ambeth.util.EqualsUtil;

public class QueryPostProcessor extends AbstractCascadePostProcessor
{
	@SuppressWarnings("unused")
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
			return null;
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
