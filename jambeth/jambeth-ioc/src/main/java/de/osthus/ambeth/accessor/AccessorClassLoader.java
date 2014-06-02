package de.osthus.ambeth.accessor;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.ConstructorAccess;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;

public class AccessorClassLoader extends ClassLoader
{
	private static final ArrayList<Reference<AccessorClassLoader>> accessClassLoaders = new ArrayList<Reference<AccessorClassLoader>>();

	private static final Lock writeLock = new ReentrantLock();

	public static AccessorClassLoader get(Class<?> type)
	{
		ClassLoader parent = type.getClassLoader();
		writeLock.lock();
		try
		{
			for (int i = accessClassLoaders.size(); i-- > 0;)
			{
				Reference<AccessorClassLoader> accessClassLoaderR = accessClassLoaders.get(i);
				AccessorClassLoader accessClassLoader = accessClassLoaderR.get();
				if (accessClassLoader == null)
				{
					// Current ClassLoader is invalidated
					accessClassLoaders.remove(i);
					continue;
				}
				if (accessClassLoader.getParent() == parent)
				{
					return accessClassLoader;
				}
			}
			AccessorClassLoader accessClassLoader = new AccessorClassLoader(parent);
			accessClassLoaders.add(new WeakReference<AccessorClassLoader>(accessClassLoader));
			return accessClassLoader;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public static void remove(ClassLoader parent)
	{
		writeLock.lock();
		try
		{
			for (int i = accessClassLoaders.size(); i-- > 0;)
			{
				Reference<AccessorClassLoader> accessClassLoaderR = accessClassLoaders.get(i);
				AccessorClassLoader accessClassLoader = accessClassLoaderR.get();
				if (accessClassLoader == null)
				{
					// Current ClassLoader is invalidated
					accessClassLoaders.remove(i);
					continue;
				}
				if (accessClassLoader.getParent() == parent)
				{
					accessClassLoaders.remove(i);
				}
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	private AccessorClassLoader(ClassLoader parent)
	{
		super(parent);
	}

	@Override
	protected java.lang.Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		writeLock.lock();
		try
		{
			// These classes come from the classloader that loaded AccessClassLoader.
			if (name.equals(FieldAccess.class.getName()))
			{
				return FieldAccess.class;
			}
			if (name.equals(MethodAccess.class.getName()))
			{
				return MethodAccess.class;
			}
			if (name.equals(ConstructorAccess.class.getName()))
			{
				return ConstructorAccess.class;
			}
			if (name.equals(AbstractAccessor.class.getName()))
			{
				return AbstractAccessor.class;
			}
			// All other classes come from the classloader that loaded the type we are accessing.
			return super.loadClass(name, resolve);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Class<?> defineClass(String name, byte[] bytes) throws ClassFormatError
	{
		try
		{
			// Attempt to load the access class in the same loader, which makes protected and default access members accessible.
			Method method = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
			method.setAccessible(true);
			return (Class<?>) method.invoke(getParent(), new Object[] { name, bytes, Integer.valueOf(0), Integer.valueOf(bytes.length) });
		}
		catch (Exception ignored)
		{
			// intended blank
		}
		return defineClass(name, bytes, 0, bytes.length);
	}
}
