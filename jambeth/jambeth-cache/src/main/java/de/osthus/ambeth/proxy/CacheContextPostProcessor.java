package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.cache.CacheContext;
import de.osthus.ambeth.cache.CacheType;
import de.osthus.ambeth.cache.config.CacheNamedBeans;
import de.osthus.ambeth.cache.interceptor.CacheContextInterceptor;
import de.osthus.ambeth.cache.interceptor.CacheInterceptor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.EqualsUtil;

public class CacheContextPostProcessor extends AbstractCascadePostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected AnnotationCache<CacheContext> annotationCache = new AnnotationCache<CacheContext>(CacheContext.class)
	{
		@Override
		protected boolean annotationEquals(CacheContext left, CacheContext right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	@Autowired
	protected CachePostProcessor cachePostProcessor;

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		CacheContext cacheContext = annotationCache.getAnnotation(type);
		if (cacheContext == null)
		{
			return null;
		}
		IMethodLevelBehavior<Annotation> cacheBehavior = cachePostProcessor.createInterceptorModeBehavior(type);

		CacheInterceptor interceptor = new CacheInterceptor();
		if (beanContext.isRunning())
		{
			interceptor = beanContext.registerWithLifecycle(interceptor)//
					.propertyValue("Behavior", cacheBehavior)//
					.ignoreProperties("ProcessService", "ServiceName")//
					.finish();
		}
		else
		{
			beanContextFactory.registerWithLifecycle(interceptor)//
					.propertyValue("Behavior", cacheBehavior)//
					.ignoreProperties("ProcessService", "ServiceName");
		}
		CacheType cacheType = cacheContext.value();
		String cacheProviderName;
		switch (cacheType)
		{
			case PROTOTYPE:
			{
				cacheProviderName = CacheNamedBeans.CacheProviderPrototype;
				break;
			}
			case SINGLETON:
			{
				cacheProviderName = CacheNamedBeans.CacheProviderSingleton;
				break;
			}
			case THREAD_LOCAL:
			{
				cacheProviderName = CacheNamedBeans.CacheProviderThreadLocal;
				break;
			}
			case DEFAULT:
			{
				return interceptor;
			}
			default:
				throw new IllegalStateException("Not supported type: " + cacheType);
		}
		CacheContextInterceptor ccInterceptor = new CacheContextInterceptor();
		if (beanContext.isRunning())
		{
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
	protected Annotation lookForAnnotation(AnnotatedElement member)
	{
		Annotation annotation = super.lookForAnnotation(member);
		if (annotation != null)
		{
			return annotation;
		}
		return member.getAnnotation(CacheContext.class);
	}
}
