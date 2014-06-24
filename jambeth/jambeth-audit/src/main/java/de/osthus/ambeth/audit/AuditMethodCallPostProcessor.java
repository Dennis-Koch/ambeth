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
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehaviour;
import de.osthus.ambeth.proxy.MethodLevelBehaviour;
import de.osthus.ambeth.proxy.MethodLevelBehaviour.IBehaviourTypeExtractor;

public class AuditMethodCallPostProcessor extends AbstractCascadePostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<AuditMethod> annotationCache = new AnnotationCache<AuditMethod>(AuditMethod.class)
	{
		@Override
		protected boolean annotationEquals(AuditMethod left, AuditMethod right)
		{
			return true;
		}
	};

	protected final IBehaviourTypeExtractor<AuditMethod, AuditMethod> auditMethodExtractor = new IBehaviourTypeExtractor<AuditMethod, AuditMethod>()
	{
		@Override
		public AuditMethod extractBehaviourType(AuditMethod annotation)
		{
			return annotation;
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		IMethodLevelBehaviour<AuditMethod> behaviour = MethodLevelBehaviour.create(type, annotationCache, AuditMethod.class, auditMethodExtractor,
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
