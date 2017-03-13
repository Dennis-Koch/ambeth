package com.koch.ambeth.util.proxy;

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;

public class ProxyFactory extends SmartCopyMap<ProxyTypeKey, Class<? extends Factory>> implements IProxyFactory
{
	protected static final Class<?>[] emptyInterfaces = new Class[0];

	protected static final Callback[] emptyCallbacks = new Callback[] { NoOp.INSTANCE };

	protected Object createProxyIntern(Class<? extends Factory> proxyType, Callback[] callbacks)
	{
		try
		{
			Factory proxy = proxyType.newInstance();
			proxy.setCallbacks(callbacks);
			return proxy;
		}
		catch (InstantiationException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		catch (IllegalAccessException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createProxy(Class<T> type, MethodInterceptor... interceptors)
	{
		ProxyTypeKey key = new ProxyTypeKey(type, emptyInterfaces);
		Class<? extends Factory> proxyType = get(key);
		if (proxyType != null)
		{
			return (T) createProxyIntern(proxyType, interceptors);
		}
		Enhancer enhancer = new Enhancer();
		enhancer.setClassLoader(Thread.currentThread().getContextClassLoader());
		if (type.isInterface())
		{
			enhancer.setInterfaces(new Class<?>[] { type });
		}
		else
		{
			enhancer.setSuperclass(type);
		}
		if (interceptors.length == 0)
		{
			enhancer.setCallbacks(emptyCallbacks);
		}
		else
		{
			enhancer.setCallbacks(interceptors);
		}
		Object proxy = enhancer.create();
		put(key, (Class<? extends Factory>) proxy.getClass());
		return (T) proxy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createProxy(Class<T> type, Class<?>[] interfaces, MethodInterceptor... interceptors)
	{
		ProxyTypeKey key = new ProxyTypeKey(type, interfaces);
		Class<? extends Factory> proxyType = get(key);
		if (proxyType != null)
		{
			return (T) createProxyIntern(proxyType, interceptors);
		}
		if (type.isInterface())
		{
			LinkedHashSet<Class<?>> allInterfaces = LinkedHashSet.create(interfaces.length + 1);
			allInterfaces.add(type);
			allInterfaces.addAll(interfaces);

			return (T) createProxy(allInterfaces.toArray(Class.class), interceptors);
		}
		ArrayList<Class<?>> tempList = new ArrayList<Class<?>>();
		for (int a = interfaces.length; a-- > 0;)
		{
			Class<?> potentialNewInterfaces = interfaces[a];
			if (!potentialNewInterfaces.isInterface())
			{
				continue;
			}
			for (int b = tempList.size(); b-- > 0;)
			{
				Class<?> existingInterface = tempList.get(b);
				if (existingInterface.isAssignableFrom(potentialNewInterfaces))
				{
					tempList.set(b, potentialNewInterfaces);
					potentialNewInterfaces = null;
					break;
				}
			}
			if (potentialNewInterfaces != null)
			{
				tempList.add(potentialNewInterfaces);
			}
		}
		Enhancer enhancer = new Enhancer();
		enhancer.setClassLoader(Thread.currentThread().getContextClassLoader());
		enhancer.setSuperclass(type);
		enhancer.setInterfaces(tempList != null ? tempList.toArray(new Class<?>[tempList.size()]) : interfaces);
		enhancer.setCallbacks(interceptors);
		Object proxy = enhancer.create();
		put(key, (Class<? extends Factory>) proxy.getClass());
		return (T) proxy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object createProxy(Class<?>[] interfaces, MethodInterceptor... interceptors)
	{
		ProxyTypeKey key = new ProxyTypeKey(Object.class, interfaces);
		Class<? extends Factory> proxyType = get(key);
		if (proxyType != null)
		{
			return createProxyIntern(proxyType, interceptors);
		}
		for (int a = 0, size = interfaces.length; a < size; a++)
		{
			Class<?> interfaceType = interfaces[a];
			if (!interfaceType.isInterface())
			{
				Class<?>[] newInterfaces = new Class<?>[interfaces.length - 1];
				System.arraycopy(interfaces, 0, newInterfaces, 0, a);
				if (interfaces.length - a > 1)
				{
					System.arraycopy(interfaces, a + 1, newInterfaces, a, interfaces.length - a - 1);
				}
				return createProxy(interfaceType, newInterfaces, interceptors);
			}
		}
		Enhancer enhancer = new Enhancer();
		enhancer.setClassLoader(Thread.currentThread().getContextClassLoader());
		enhancer.setInterfaces(interfaces);
		enhancer.setCallbacks(interceptors);
		Object proxy = enhancer.create();
		put(key, (Class<? extends Factory>) proxy.getClass());
		return proxy;
	}

	@Override
	public ICascadedInterceptor wrap(Object target, ICascadedInterceptor interceptor)
	{
		interceptor.setTarget(target);
		return interceptor;
	}
}
