package de.osthus.ambeth.audit;

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

public class AuditMethodCallPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanPostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<Audited> annotationCache = new AnnotationCache<Audited>(Audited.class)
	{
		@Override
		protected boolean annotationEquals(Audited left, Audited right)
		{
			return true;
		}
	};

	protected final IBehaviorTypeExtractor<Audited, Audited> auditMethodExtractor = new IBehaviorTypeExtractor<Audited, Audited>()
	{
		@Override
		public Audited extractBehaviorType(Audited annotation)
		{
			return annotation;
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		IMethodLevelBehavior<Audited> behaviour = MethodLevelBehavior.create(type, annotationCache, Audited.class, auditMethodExtractor,
				beanContextFactory, beanContext);
		if (behaviour == null)
		{
			return null;
		}
		AuditMethodCallInterceptor interceptor = new AuditMethodCallInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<AuditMethodCallInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
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
		return PostProcessorOrder.LOW;
	}
}
