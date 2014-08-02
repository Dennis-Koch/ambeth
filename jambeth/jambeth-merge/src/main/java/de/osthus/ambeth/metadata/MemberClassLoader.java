package de.osthus.ambeth.metadata;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MemberClassLoader extends ClassLoader
{
	private static final ArrayList<Reference<MemberClassLoader>> accessClassLoaders = new ArrayList<Reference<MemberClassLoader>>();

	private static final Lock writeLock = new ReentrantLock();

	public static MemberClassLoader get(Class<?> type)
	{
		ClassLoader parent = type.getClassLoader();
		writeLock.lock();
		try
		{
			for (int i = accessClassLoaders.size(); i-- > 0;)
			{
				Reference<MemberClassLoader> accessClassLoaderR = accessClassLoaders.get(i);
				MemberClassLoader accessClassLoader = accessClassLoaderR.get();
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
			MemberClassLoader accessClassLoader = new MemberClassLoader(parent);
			accessClassLoaders.add(new WeakReference<MemberClassLoader>(accessClassLoader));
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
				Reference<MemberClassLoader> accessClassLoaderR = accessClassLoaders.get(i);
				MemberClassLoader accessClassLoader = accessClassLoaderR.get();
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

	private MemberClassLoader(ClassLoader parent)
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
			if (name.equals(Member.class.getName()))
			{
				return Member.class;
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
