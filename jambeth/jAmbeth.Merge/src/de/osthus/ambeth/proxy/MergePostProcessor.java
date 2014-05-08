package de.osthus.ambeth.proxy;

import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.interceptor.MergeInterceptor;

public class MergePostProcessor extends AbstractCascadePostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<MergeContext> mergeContextCache = new AnnotationCache<MergeContext>(MergeContext.class)
	{
		@Override
		protected boolean annotationEquals(MergeContext left, MergeContext right)
		{
			// No individual properties for this annotation
			return false;
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		MergeContext mergeContext = mergeContextCache.getAnnotation(type);
		if (mergeContext == null)
		{
			return null;
		}
		MergeInterceptor mergeInterceptor = new MergeInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<MergeInterceptor> interceptorBC = beanContext.registerWithLifecycle(mergeInterceptor);
			return interceptorBC.finish();
		}
		beanContextFactory.registerWithLifecycle(mergeInterceptor);
		return mergeInterceptor;
	}
}
