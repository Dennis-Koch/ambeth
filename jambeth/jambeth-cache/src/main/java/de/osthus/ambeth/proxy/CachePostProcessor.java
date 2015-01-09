package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.annotation.AnnotationEntry;
import de.osthus.ambeth.cache.Cached;
import de.osthus.ambeth.cache.interceptor.CacheInterceptor;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.service.IServiceExtendable;
import de.osthus.ambeth.util.EqualsUtil;

public class CachePostProcessor extends MergePostProcessor
{
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<Service> serviceAnnotationCache = new AnnotationCache<Service>(Service.class)
	{
		@Override
		protected boolean annotationEquals(Service left, Service right)
		{
			return EqualsUtil.equals(left.value(), right.value()) && EqualsUtil.equals(left.name(), right.name());
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

	@Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean isNetworkClientMode;

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		Service serviceAnnotation = serviceAnnotationCache.getAnnotation(type);
		if (serviceAnnotation != null)
		{
			return handleServiceAnnotation(serviceAnnotation, beanContextFactory, beanContext, beanConfiguration, type);
		}
		AnnotationEntry<ServiceClient> serviceClientAnnotation = serviceClientAnnotationCache.getAnnotationEntry(type);
		if (serviceClientAnnotation != null)
		{
			return handleServiceClientAnnotation(serviceClientAnnotation, beanContextFactory, beanContext, beanConfiguration, type);
		}
		return super.handleServiceIntern(beanContextFactory, beanContext, beanConfiguration, type, requestedTypes);
	}

	protected String extractServiceName(IServiceContext beanContext, String serviceName, Class<?> type)
	{
		if (serviceName == null || serviceName.length() == 0)
		{
			serviceName = type.getSimpleName();
			if (serviceName.endsWith("Proxy"))
			{
				serviceName = serviceName.substring(0, serviceName.length() - 5);
			}
			if (serviceName.charAt(0) == 'I' && Character.isUpperCase(serviceName.charAt(1)))
			{
				serviceName = serviceName.substring(1);
			}
		}
		return serviceName;
	}

	protected ICascadedInterceptor handleServiceAnnotation(Service serviceAnnotation, IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type)
	{
		if (serviceAnnotation.customExport())
		{
			// Do nothing if the service wants to be exported by some special way anywhere else
			return null;
		}
		String beanName = beanConfiguration.getName();
		String serviceName = extractServiceName(beanContext, serviceAnnotation.name(), type);
		if (!isNetworkClientMode)
		{
			IMethodLevelBehavior<Annotation> behavior = createInterceptorModeBehavior(type);

			CacheInterceptor interceptor = new CacheInterceptor();
			if (beanContext.isRunning())
			{
				interceptor = beanContext.registerWithLifecycle(interceptor).propertyValue("ServiceName", serviceName).propertyValue("Behavior", behavior)
						.ignoreProperties("ProcessService").finish();
				beanContext.link(beanName).to(IServiceExtendable.class).with(serviceName);
			}
			else
			{
				beanContextFactory.registerWithLifecycle(interceptor).propertyValue("ServiceName", serviceName).propertyValue("Behavior", behavior)
						.ignoreProperties("ProcessService");
				beanContextFactory.link(beanName).to(IServiceExtendable.class).with(serviceName);
			}
			if (log.isInfoEnabled())
			{
				log.info("Registering application service '" + serviceName + "'");
			}
			return interceptor;
		}
		else
		{
			if (log.isInfoEnabled())
			{
				log.info("Registering application client stub '" + serviceName + "'");
			}
			if (beanContext.isRunning())
			{
				beanContext.link(beanName).to(IServiceExtendable.class).with(serviceName);
			}
			else
			{
				beanContextFactory.link(beanName).to(IServiceExtendable.class).with(serviceName);
			}
			return null;
		}
	}

	protected ICascadedInterceptor handleServiceClientAnnotation(AnnotationEntry<ServiceClient> serviceClientAnnotation,
			IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type)
	{
		String serviceName = extractServiceName(beanContext, serviceClientAnnotation.getAnnotation().value(), serviceClientAnnotation.getDeclaringType());

		IMethodLevelBehavior<Annotation> behavior = createInterceptorModeBehavior(type);

		CacheInterceptor interceptor = new CacheInterceptor();
		if (beanContext != null)
		{
			interceptor = beanContext.registerWithLifecycle(interceptor).propertyValue("ServiceName", serviceName).propertyValue("Behavior", behavior).finish();
			// beanContext.link(cacheInterceptorName).to(ICacheServiceByNameExtendable.class).with(serviceName);
		}
		else
		{
			beanContextFactory.registerWithLifecycle(interceptor).propertyValue("ServiceName", serviceName).propertyValue("Behavior", behavior);
			// beanContextFactory.link(cacheInterceptorName).to(ICacheServiceByNameExtendable.class).with(serviceName);
		}

		if (log.isInfoEnabled())
		{
			log.info("Creating application service stub for service '" + serviceName + "' accessing with '"
					+ serviceClientAnnotation.getDeclaringType().getName() + "'");
		}
		return interceptor;
	}

	protected String buildCacheInterceptorName(String serviceName)
	{
		return "cacheInterceptor." + serviceName;
	}

	@Override
	protected Annotation lookForAnnotation(AnnotatedElement member)
	{
		Annotation annotation = super.lookForAnnotation(member);
		if (annotation != null)
		{
			return annotation;
		}
		return member.getAnnotation(Cached.class);
	}
}
