package de.osthus.ambeth.security.proxy;

import java.lang.reflect.AnnotatedElement;
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
import de.osthus.ambeth.proxy.IBehaviorTypeExtractor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;
import de.osthus.ambeth.proxy.MethodLevelBehavior;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.security.SecurityFilterInterceptor;
import de.osthus.ambeth.util.EqualsUtil;

public class SecurityPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanPostProcessor
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

	protected final IBehaviorTypeExtractor<SecurityContext, SecurityContextType> btExtractor = new IBehaviorTypeExtractor<SecurityContext, SecurityContextType>()
	{
		@Override
		public SecurityContextType extractBehaviorType(SecurityContext annotation, AnnotatedElement annotatedElement)
		{
			return annotation.value();
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		IMethodLevelBehavior<SecurityContextType> behaviour = MethodLevelBehavior.create(type, annotationCache, SecurityContextType.class, btExtractor,
				beanContextFactory, beanContext);
		if (behaviour == null)
		{
			return null;
		}
		SecurityFilterInterceptor interceptor = new SecurityFilterInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<SecurityFilterInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
			interceptorBC.propertyValue("MethodLevelBehaviour", behaviour);
			return interceptorBC.finish();
		}
		IBeanConfiguration interceptorBC = beanContextFactory.registerWithLifecycle(interceptor);
		interceptorBC.propertyValue("MethodLevelBehaviour", behaviour);
		return interceptor;

	}

	@Override
	public PostProcessorOrder getOrder()
	{
		return PostProcessorOrder.HIGHER;
	}
}
