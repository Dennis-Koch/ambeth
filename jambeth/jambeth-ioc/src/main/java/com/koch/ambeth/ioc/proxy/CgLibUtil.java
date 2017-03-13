package com.koch.ambeth.ioc.proxy;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;

import net.sf.cglib.proxy.Enhancer;

public class CgLibUtil implements ICgLibUtil, IDisposableBean
{
	public static class ClassReference extends WeakReference<Class<?>>
	{
		private final String name;

		public ClassReference(Class<?> referent, ReferenceQueue<Class<?>> q, String name)
		{
			super(referent, q);
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	protected final HashMap<String, Boolean> typeToEnhancedMap = new HashMap<String, Boolean>();

	protected final HashMap<String, ClassReference> typeToOriginalMap = new HashMap<String, ClassReference>();

	protected final ReferenceQueue<Class<?>> classQueue = new ReferenceQueue<Class<?>>();

	protected final Lock tteLock = new ReentrantLock();

	@Override
	public void destroy() throws Throwable
	{
		tteLock.lock();
		try
		{
			typeToEnhancedMap.clear();
			typeToOriginalMap.clear();
			checkForCleanup();
		}
		finally
		{
			tteLock.unlock();
		}
	}

	@Override
	public boolean isEnhanced(Class<?> enhancedClass)
	{
		tteLock.lock();
		try
		{
			String className = enhancedClass.getName();
			Boolean enhanced = typeToEnhancedMap.get(className);
			if (enhanced == null)
			{

				enhanced = Boolean.valueOf(Enhancer.isEnhanced(enhancedClass) || Proxy.isProxyClass(enhancedClass)
				// || ProxyObject.class.isAssignableFrom(enhancedClass)
						);
				typeToEnhancedMap.put(className, enhanced);
			}
			return enhanced.booleanValue();
		}
		finally
		{
			tteLock.unlock();
		}
	}

	@Override
	public Class<?> getOriginalClass(Class<?> enhancedClass)
	{
		tteLock.lock();
		try
		{
			String className = enhancedClass.getName();
			ClassReference originalR = typeToOriginalMap.get(className);
			Class<?> original = null;
			if (originalR != null)
			{
				original = originalR.get();
			}
			if (original == null)
			{
				original = enhancedClass;
				while (isEnhanced(original))
				{
					Class<?> superClass = original.getSuperclass();
					if (Object.class.equals(superClass))
					{
						original = original.getInterfaces()[0];
						break;
					}
					original = original.getSuperclass();
				}
				typeToOriginalMap.put(className, new ClassReference(original, classQueue, className));
			}
			checkForCleanup();
			return original;
		}
		finally
		{
			tteLock.unlock();
		}
	}

	@Override
	public Class<?>[] getAllInterfaces(Object obj, Class<?>... additional)
	{
		ISet<Class<?>> interfaceSet = new HashSet<Class<?>>();
		Class<?> currType = obj.getClass();
		while (currType != null)
		{
			Class<?>[] interfaces = currType.getInterfaces();
			for (int a = interfaces.length; a-- > 0;)
			{
				interfaceSet.add(interfaces[a]);
			}
			currType = currType.getSuperclass();
		}
		for (int a = additional.length; a-- > 0;)
		{
			interfaceSet.add(additional[a]);
		}
		return interfaceSet.toArray(new Class<?>[interfaceSet.size()]);
	}

	protected void checkForCleanup()
	{
		ClassReference classR;
		while ((classR = (ClassReference) classQueue.poll()) != null)
		{
			String name = classR.getName();
			typeToEnhancedMap.remove(name);
			typeToOriginalMap.remove(name);
		}
	}
}
