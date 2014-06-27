package de.osthus.ambeth.audit;

import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.AbstractCascadePostProcessor;
import de.osthus.ambeth.proxy.IBehaviorTypeExtractor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;
import de.osthus.ambeth.proxy.MethodLevelBehavior;

public class AuditMethodCallPostProcessor extends AbstractCascadePostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<AuditAccess> annotationCache = new AnnotationCache<AuditAccess>(AuditAccess.class)
	{
		@Override
		protected boolean annotationEquals(AuditAccess left, AuditAccess right)
		{
			return true;
		}
	};

	protected final IBehaviorTypeExtractor<AuditAccess, AuditAccess> auditMethodExtractor = new IBehaviorTypeExtractor<AuditAccess, AuditAccess>()
	{
		@Override
		public AuditAccess extractBehaviorType(AuditAccess annotation)
		{
			return annotation;
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		IMethodLevelBehavior<AuditAccess> behaviour = MethodLevelBehavior.create(type, annotationCache, AuditAccess.class, auditMethodExtractor,
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
}
