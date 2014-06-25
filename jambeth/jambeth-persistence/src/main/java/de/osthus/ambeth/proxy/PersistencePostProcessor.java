package de.osthus.ambeth.proxy;

import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext.PersistenceContextType;
import de.osthus.ambeth.util.EqualsUtil;

public class PersistencePostProcessor extends AbstractCascadePostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<PersistenceContext> annotationCache = new AnnotationCache<PersistenceContext>(PersistenceContext.class)
	{
		@Override
		protected boolean annotationEquals(PersistenceContext left, PersistenceContext right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	protected final IBehaviorTypeExtractor<PersistenceContext, PersistenceContextType> btExtractor = new IBehaviorTypeExtractor<PersistenceContext, PersistenceContextType>()
	{
		@Override
		public PersistenceContextType extractBehaviorType(PersistenceContext annotation)
		{
			return annotation.value();
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		IMethodLevelBehavior<PersistenceContextType> behaviour = MethodLevelBehavior.create(type, annotationCache, PersistenceContextType.class, btExtractor,
				beanContextFactory, beanContext);
		if (behaviour == null)
		{
			return null;
		}
		PersistenceContextInterceptor interceptor = new PersistenceContextInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<PersistenceContextInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
			interceptorBC.propertyValue("MethodLevelBehaviour", behaviour);
			return interceptorBC.finish();
		}
		IBeanConfiguration interceptorBC = beanContextFactory.registerWithLifecycle(interceptor);
		interceptorBC.propertyValue("MethodLevelBehaviour", behaviour);
		return interceptor;
	}
}
