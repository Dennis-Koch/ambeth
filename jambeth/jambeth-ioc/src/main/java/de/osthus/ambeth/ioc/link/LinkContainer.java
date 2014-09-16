package de.osthus.ambeth.ioc.link;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.reflect.FastMethod;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.extendable.IExtendableRegistry;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.proxy.DelegateInterceptor;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ParamHolder;
import de.osthus.ambeth.util.ReflectUtil;

public class LinkContainer extends AbstractLinkContainer
{
	@LogInstance
	private ILogger log;

	protected IExtendableRegistry extendableRegistry;

	protected IProxyFactory proxyFactory;

	protected FastMethod addMethod;

	protected FastMethod removeMethod;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(extendableRegistry, "ExtendableRegistry");
		ParamChecker.assertNotNull(proxyFactory, "ProxyFactory");
	}

	public void setExtendableRegistry(IExtendableRegistry extendableRegistry)
	{
		this.extendableRegistry = extendableRegistry;
	}

	public void setProxyFactory(IProxyFactory proxyFactory)
	{
		this.proxyFactory = proxyFactory;
	}

	@Override
	protected Object resolveRegistryIntern(Object registry)
	{
		registry = super.resolveRegistryIntern(registry);
		ParamHolder<Object[]> linkArgumentsPH = new ParamHolder<Object[]>();
		FastMethod[] methods;
		if (registryPropertyName != null)
		{
			methods = extendableRegistry.getAddRemoveMethods(registry.getClass(), registryPropertyName, arguments, linkArgumentsPH);
		}
		else
		{
			methods = extendableRegistry.getAddRemoveMethods(registryBeanAutowiredType, arguments, linkArgumentsPH);
		}
		arguments = linkArgumentsPH.getValue();
		addMethod = methods[0];
		removeMethod = methods[1];
		return registry;
	}

	@Override
	protected Object resolveListenerIntern(Object listener)
	{
		listener = super.resolveListenerIntern(listener);
		if (listenerMethodName == null)
		{
			return listener;
		}
		Class<?> parameterType = addMethod.getParameterTypes()[0];
		Method[] methodsOnExpectedListenerType = ReflectUtil.getDeclaredMethods(parameterType);
		HashMap<Method, Method> mappedMethods = new HashMap<Method, Method>();
		for (Method methodOnExpectedListenerType : methodsOnExpectedListenerType)
		{
			Annotation[][] parameterAnnotations = methodOnExpectedListenerType.getParameterAnnotations();
			Class<?>[] types = methodOnExpectedListenerType.getParameterTypes();

			CascadedInterceptor cascadedInterceptor = null;
			if (listener instanceof Factory)
			{
				Callback[] callbacks = ((Factory) listener).getCallbacks();
				if (callbacks != null && callbacks.length == 1)
				{
					Callback callback = callbacks[0];
					if (callback instanceof CascadedInterceptor)
					{
						cascadedInterceptor = (CascadedInterceptor) callback;
						Object target = cascadedInterceptor;
						while (target instanceof CascadedInterceptor)
						{
							Object targetOfTarget = ((CascadedInterceptor) target).getTarget();
							if (targetOfTarget != null)
							{
								target = targetOfTarget;
							}
							else
							{
								target = null;
								break;
							}
						}
						if (target != null)
						{
							listener = target;
						}
					}
				}
			}
			Method method = null;
			while (method == null)
			{
				method = ReflectUtil.getDeclaredMethod(true, listener.getClass(), methodOnExpectedListenerType.getReturnType(), listenerMethodName, types);
				if (method == null && types.length > 0)
				{
					Class<?> firstType = types[0];
					types[0] = null;
					method = ReflectUtil.getDeclaredMethod(true, listener.getClass(), methodOnExpectedListenerType.getReturnType(), listenerMethodName, types);
					types[0] = firstType;
				}
				if (method != null)
				{
					break;
				}
				if (types.length > 1)
				{
					Annotation[] annotationsOfLastType = parameterAnnotations[types.length - 1];
					LinkOptional linkOptional = null;
					for (Annotation annotationOfLastType : annotationsOfLastType)
					{
						if (annotationOfLastType instanceof LinkOptional)
						{
							linkOptional = (LinkOptional) annotationOfLastType;
							break;
						}
					}
					if (linkOptional != null)
					{
						// drop last expected argument and look again
						Class<?>[] newTypes = new Class<?>[types.length - 1];
						System.arraycopy(types, 0, newTypes, 0, newTypes.length);
						types = newTypes;
						continue;
					}
				}
				throw new IllegalArgumentException("Could not map given method '" + listenerMethodName + "' of listener " + listener + " to signature: "
						+ methodOnExpectedListenerType);
			}
			mappedMethods.put(methodOnExpectedListenerType, method);
		}
		MethodInterceptor interceptor = new DelegateInterceptor(listener, mappedMethods);
		listener = proxyFactory.createProxy(parameterType, listener.getClass().getInterfaces(), interceptor);
		return listener;
	}

	protected void evaluateRegistryMethods(Object registry)
	{
	}

	@Override
	protected ILogger getLog()
	{
		return log;
	}

	@Override
	protected void handleLink(Object registry, Object listener)
	{
		evaluateRegistryMethods(registry);
		arguments[0] = listener;
		try
		{
			this.addMethod.invoke(registry, arguments);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected void handleUnlink(Object registry, Object listener)
	{
		arguments[0] = listener;
		try
		{
			this.removeMethod.invoke(registry, arguments);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
