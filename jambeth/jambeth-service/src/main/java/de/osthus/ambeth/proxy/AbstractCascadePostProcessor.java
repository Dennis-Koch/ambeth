package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import de.osthus.ambeth.ioc.IBeanPostProcessor;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public abstract class AbstractCascadePostProcessor implements IBeanPostProcessor, IInitializingBean
{
	@LogInstance
	private ILogger log;

	private static final Class<?>[] emptyClasses = new Class<?>[0];

	protected IProxyFactory proxyFactory;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(proxyFactory, "ProxyFactory");
	}

	public void setProxyFactory(IProxyFactory proxyFactory)
	{
		this.proxyFactory = proxyFactory;
	}

	@Override
	public Object postProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
			Object targetBean, Set<Class<?>> requestedTypes)
	{
		Factory factory = null;
		ICascadedInterceptor cascadedInterceptor = null;
		Object proxiedTargetBean = targetBean;
		if (targetBean instanceof Factory)
		{
			factory = (Factory) targetBean;
			Callback callback = factory.getCallback(0);
			if (callback instanceof ICascadedInterceptor)
			{
				cascadedInterceptor = (ICascadedInterceptor) callback;
				proxiedTargetBean = cascadedInterceptor.getTarget();
			}
		}
		ICascadedInterceptor interceptor = handleServiceIntern(beanContextFactory, beanContext, beanConfiguration, beanType, requestedTypes);
		if (interceptor == null)
		{
			return targetBean;
		}
		if (log.isDebugEnabled())
		{
			log.debug(getClass().getSimpleName() + " intercepted bean with name '" + beanConfiguration.getName() + "'");
		}
		if (cascadedInterceptor == null)
		{
			ICascadedInterceptor lastInterceptor = interceptor;
			while (lastInterceptor.getTarget() instanceof ICascadedInterceptor)
			{
				lastInterceptor = (ICascadedInterceptor) lastInterceptor.getTarget();
			}
			lastInterceptor.setTarget(proxiedTargetBean);
			Object proxy;
			if (requestedTypes.size() == 0)
			{
				proxy = proxyFactory.createProxy(beanType, emptyClasses, interceptor);
			}
			else
			{
				proxy = proxyFactory.createProxy(requestedTypes.toArray(new Class[requestedTypes.size()]), interceptor);
			}
			postHandleServiceIntern(beanContextFactory, beanContext, beanConfiguration, beanType, requestedTypes, proxy);
			return proxy;
		}
		interceptor.setTarget(cascadedInterceptor);
		factory.setCallback(0, interceptor);
		return targetBean;
	}

	protected void postHandleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration,
			Class<?> type, Set<Class<?>> requestedTypes, Object proxy)
	{
		// Intended blank
	}

	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		return null;
	}

	public IMethodLevelBehavior<Annotation> createInterceptorModeBehavior(Class<?> beanType)
	{
		MethodLevelHashMap<Annotation> methodToAnnotationMap = new MethodLevelHashMap<Annotation>();
		Method[] methods = ReflectUtil.getMethods(beanType);
		for (Method method : methods)
		{
			Annotation annotation = lookForAnnotation(method);
			if (annotation != null)
			{
				methodToAnnotationMap.put(method.getName(), method.getParameterTypes(), annotation);
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
				methodToAnnotationMap.put(method.getName(), method.getParameterTypes(), annotation);
				break;
			}
		}
		return new MethodLevelBehavior<Annotation>(lookForAnnotation(beanType), methodToAnnotationMap);
	}

	protected Annotation lookForAnnotation(AnnotatedElement member)
	{
		return null;
	}
}
