package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
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
import de.osthus.ambeth.util.MethodKey;
import de.osthus.ambeth.util.ReflectUtil;

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

	protected IMethodLevelBehavior<Annotation> createInterceptorModeBehavior(Class<?> beanType)
	{
		HashMap<MethodKey, Annotation> methodToAnnotationMap = new HashMap<MethodKey, Annotation>();
		Method[] methods = ReflectUtil.getMethods(beanType);
		for (Method method : methods)
		{
			Annotation annotation = lookForAnnotation(method);
			if (annotation != null)
			{
				methodToAnnotationMap.put(new MethodKey(method.getName(), method.getParameterTypes()), annotation);
				continue;
			}
			for (Class<?> currInterface : beanType.getInterfaces())
			{
				Method methodOnInterface = ReflectUtil.getDeclaredMethod(true, currInterface, null, method.getName(), method.getParameterTypes());
				if (methodOnInterface == null)
				{
					continue;
				}
				annotation = lookForAnnotation(methodOnInterface);
				if (annotation == null)
				{
					continue;
				}
				methodToAnnotationMap.put(new MethodKey(method.getName(), method.getParameterTypes()), annotation);
				break;
			}
		}
		return new MethodLevelBehavior<Annotation>(null, methodToAnnotationMap);
	}

	protected Annotation lookForAnnotation(Method method)
	{
		NoProxy noProxy = method.getAnnotation(NoProxy.class);
		if (noProxy != null)
		{
			return noProxy;
		}
		de.osthus.ambeth.annotation.Process process = method.getAnnotation(de.osthus.ambeth.annotation.Process.class);
		if (process != null)
		{
			return process;
		}
		Find find = method.getAnnotation(Find.class);
		if (find != null)
		{
			return find;
		}
		Merge merge = method.getAnnotation(Merge.class);
		if (merge != null)
		{
			return merge;
		}
		return method.getAnnotation(Remove.class);
	}
}
