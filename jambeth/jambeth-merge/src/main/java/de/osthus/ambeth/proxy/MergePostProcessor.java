package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.Merge;
import de.osthus.ambeth.annotation.NoProxy;
import de.osthus.ambeth.annotation.Remove;
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
			return true;
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
		IMethodLevelBehavior<Annotation> behavior = createInterceptorModeBehavior(type);

		MergeInterceptor mergeInterceptor = new MergeInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<MergeInterceptor> interceptorBC = beanContext.registerWithLifecycle(mergeInterceptor);
			interceptorBC.propertyValue("Behavior", behavior);
			return interceptorBC.finish();
		}
		beanContextFactory.registerWithLifecycle(mergeInterceptor).propertyValue("Behavior", behavior);
		return mergeInterceptor;
	}

	@Override
	protected Annotation lookForAnnotation(AnnotatedElement member)
	{
		Annotation annotation = super.lookForAnnotation(member);
		if (annotation != null)
		{
			return annotation;
		}
		NoProxy noProxy = member.getAnnotation(NoProxy.class);
		if (noProxy != null)
		{
			return noProxy;
		}
		de.osthus.ambeth.annotation.Process process = member.getAnnotation(de.osthus.ambeth.annotation.Process.class);
		if (process != null)
		{
			return process;
		}
		Find find = member.getAnnotation(Find.class);
		if (find != null)
		{
			return find;
		}
		Merge merge = member.getAnnotation(Merge.class);
		if (merge != null)
		{
			return merge;
		}
		return member.getAnnotation(Remove.class);
	}
}
