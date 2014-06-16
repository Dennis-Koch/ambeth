package de.osthus.ambeth.security.proxy;

import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IOrderedBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.PostProcessorOrder;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.AbstractCascadePostProcessor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehaviour;
import de.osthus.ambeth.proxy.MethodLevelBehaviour;
import de.osthus.ambeth.proxy.MethodLevelBehaviour.IBehaviourTypeExtractor;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.security.SecurityFilterInterceptor;
import de.osthus.ambeth.util.EqualsUtil;

public class CopyOfSecurityPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanPostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected AnnotationCache<SecurityContext> annotationCache = new AnnotationCache<SecurityContext>(SecurityContext.class)
	{
		@Override
		protected boolean annotationEquals(SecurityContext left, SecurityContext right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	protected final IBehaviourTypeExtractor<SecurityContext, SecurityContextType> btExtractor = new IBehaviourTypeExtractor<SecurityContext, SecurityContextType>()
	{
		@Override
		public SecurityContextType extractBehaviourType(SecurityContext annotation)
		{
			return annotation.value();
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		IMethodLevelBehaviour<SecurityContextType> behavior = getBehavior(beanContextFactory, beanContext, beanConfiguration, type, requestedTypes);
		if (behavior == null)
		{
			return null;
		}
		SecurityFilterInterceptor interceptor = new SecurityFilterInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<SecurityFilterInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
			return configure(interceptorBC, behavior).finish();
		}
		IBeanConfiguration interceptorBC = beanContextFactory.registerWithLifecycle(interceptor);
		interceptorBC = configure(interceptorBC, behavior);
		return interceptor;
	}

	protected IMethodLevelBehaviour<SecurityContextType> getBehavior(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		return MethodLevelBehaviour.create(type, annotationCache, SecurityContextType.class, btExtractor, beanContextFactory, beanContext);
	}

	protected IBeanRuntime<SecurityFilterInterceptor> configure(IBeanRuntime<SecurityFilterInterceptor> interceptorBC,
			IMethodLevelBehaviour<SecurityContextType> behavior)
	{
		return interceptorBC.propertyValue("MethodLevelBehaviour", behavior);
	}

	protected IBeanConfiguration configure(IBeanConfiguration interceptorBC, IMethodLevelBehaviour<SecurityContextType> behavior)
	{
		return interceptorBC.propertyValue("MethodLevelBehaviour", behavior);
	}

	@Override
	public PostProcessorOrder getOrder()
	{
		// A security filter interceptor has to be one of the OUTERMOST proxies of all potential postprocessor-created proxies
		// The proxy order is the inverse order of their creating postprocessors
		return PostProcessorOrder.LOW;
	}
}
