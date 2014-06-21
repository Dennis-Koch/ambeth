package de.osthus.ambeth.cache;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IOrderedBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.PostProcessorOrder;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.proxy.AbstractCascadePostProcessor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehaviour;
import de.osthus.ambeth.proxy.MethodLevelBehaviour;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.security.SecurityFilterInterceptor;
import de.osthus.ambeth.typeinfo.TypeInfoItemUtil;
import de.osthus.ambeth.util.MethodKey;
import de.osthus.ambeth.util.ReflectUtil;

public class CommittedRootCachePostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanPostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		if (!CacheModule.COMMITTED_ROOT_CACHE.equals(beanConfiguration.getName()))
		{
			return null;
		}
		Map<MethodKey, SecurityContextType> methodLevelBehaviour = null;

		Method[] methods = ReflectUtil.getMethods(type);
		for (int a = methods.length; a-- > 0;)
		{
			Method method = methods[a];

			if ((method.getModifiers() & Modifier.PUBLIC) == 0 || void.class.equals(method.getReturnType()))
			{
				// ignore this method
				continue;
			}
			Class<?> elementReturnType = TypeInfoItemUtil.getElementTypeUsingReflection(method.getReturnType(), method.getGenericReturnType());
			if (!IObjRef.class.isAssignableFrom(elementReturnType) && !IObjRelationResult.class.isAssignableFrom(elementReturnType)
					&& !Object.class.equals(elementReturnType))
			{
				// ignore this method
				continue;
			}
			if (method.getName().equals("createCacheValueInstance"))
			{
				// ignore this method
				continue;
			}
			if (methodLevelBehaviour == null)
			{
				methodLevelBehaviour = new HashMap<MethodKey, SecurityContextType>();
			}
			MethodKey methodKey = new MethodKey(method.getName(), method.getParameterTypes());
			methodLevelBehaviour.put(methodKey, SecurityContextType.AUTHENTICATED);
		}
		if (methodLevelBehaviour == null)
		{
			methodLevelBehaviour = Collections.<MethodKey, SecurityContextType> emptyMap();
		}
		IMethodLevelBehaviour<SecurityContextType> behaviour = new MethodLevelBehaviour<SecurityContextType>(SecurityContextType.NOT_REQUIRED,
				methodLevelBehaviour);

		SecurityFilterInterceptor interceptor = new SecurityFilterInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<SecurityFilterInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
			interceptorBC.propertyValue("MethodLevelBehaviour", behaviour).propertyValue(SecurityFilterInterceptor.PROP_CHECK_METHOD_ACCESS, Boolean.FALSE);
			return interceptorBC.finish();
		}
		IBeanConfiguration interceptorBC = beanContextFactory.registerWithLifecycle(interceptor);
		interceptorBC.propertyValue("MethodLevelBehaviour", behaviour).propertyValue(SecurityFilterInterceptor.PROP_CHECK_METHOD_ACCESS, Boolean.FALSE);
		return interceptor;

	}

	@Override
	public PostProcessorOrder getOrder()
	{
		// A security filter interceptor has to be one of the OUTERMOST proxies of all potential postprocessor-created proxies
		// The proxy order is the inverse order of their creating postprocessors
		return PostProcessorOrder.LOW;
	}
}
