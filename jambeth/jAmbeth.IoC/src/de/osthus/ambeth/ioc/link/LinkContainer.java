package de.osthus.ambeth.ioc.link;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.reflect.FastMethod;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.extendable.IExtendableRegistry;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
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
		if (listenerMethodName != null)
		{
			Class<?> parameterType = addMethod.getParameterTypes()[0];
			Method[] methodsOnExpectedListenerType = ReflectUtil.getDeclaredMethods(parameterType);
			HashMap<Method, Method> mappedMethods = new HashMap<Method, Method>();
			for (Method methodOnExpectedListenerType : methodsOnExpectedListenerType)
			{
				Class<?>[] types = methodOnExpectedListenerType.getParameterTypes();
				Method method;
				try
				{
					method = listener.getClass().getDeclaredMethod(listenerMethodName, types);
					method.setAccessible(true);
				}
				catch (SecurityException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				catch (NoSuchMethodException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				if (method != null)
				{
					mappedMethods.put(methodOnExpectedListenerType, method);
				}
			}
			MethodInterceptor interceptor = new DelegateInterceptor(listener, mappedMethods);
			listener = proxyFactory.createProxy(parameterType, interceptor);
		}
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
