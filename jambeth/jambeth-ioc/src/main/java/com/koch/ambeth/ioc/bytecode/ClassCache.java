package com.koch.ambeth.ioc.bytecode;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IInterningFeature;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.WeakSmartCopyMap;

public class ClassCache implements IClassCache {

	private static class CacheEntry {
		private final Lock writeLock = new ReentrantLock();

		private final HashMap<String, Reference<Class<?>>> resolvedClassMap = new HashMap<>();
	}

	@Autowired
	protected IClassLoaderProvider classLoaderProvider;

	@Autowired
	protected IInterningFeature interningFeature;

	protected final WeakSmartCopyMap<ClassLoader, CacheEntry> classLoaderToCacheEntryMap =
			new WeakSmartCopyMap<>();

	public void setClassLoaderProvider(IClassLoaderProvider classLoaderProvider) {
		this.classLoaderProvider = classLoaderProvider;
	}

	public void setInterningFeature(IInterningFeature interningFeature) {
		this.interningFeature = interningFeature;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, classLoaderProvider.getClassLoader());
	}

	@Override
	public Class<?> loadClass(String name, ClassLoader classLoader) throws ClassNotFoundException {

		CacheEntry cacheEntry = getEnsureCacheEntry(classLoader);
		Class<?> type = getCachedClassEntry(name, cacheEntry);
		if (type != null) {
			return type;
		}
		type = classLoader.loadClass(name);
		return cacheClassEntry(name, cacheEntry, type);
	}

	@Override
	public Class<?> forName(String name) throws ClassNotFoundException {
		return forName(name, classLoaderProvider.getClassLoader());
	}

	@Override
	public Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException {
		CacheEntry cacheEntry = getEnsureCacheEntry(classLoader);
		Class<?> type = getCachedClassEntry(name, cacheEntry);
		if (type != null) {
			return type;
		}
		type = Class.forName(name, true, classLoader);
		return cacheClassEntry(name, cacheEntry, type);
	}

	@Override
	public void invalidate(ClassLoader classLoader) {
		classLoaderToCacheEntryMap.remove(classLoader);
	}

	private CacheEntry getEnsureCacheEntry(ClassLoader classLoader) {
		CacheEntry cacheEntry = classLoaderToCacheEntryMap.get(classLoader);
		if (cacheEntry == null) {
			cacheEntry = new CacheEntry();
			if (!classLoaderToCacheEntryMap.putIfNotExists(classLoader, cacheEntry)) {
				cacheEntry = classLoaderToCacheEntryMap.get(classLoader);
			}
		}
		return cacheEntry;
	}

	private Class<?> cacheClassEntry(String name, CacheEntry cacheEntry, Class<?> type) {
		Lock writeLock = cacheEntry.writeLock;
		writeLock.lock();
		try {
			Reference<Class<?>> typeR = cacheEntry.resolvedClassMap.get(name);
			Class<?> existingType = typeR != null ? typeR.get() : null;
			if (existingType != null) {
				return existingType;
			}
			cacheEntry.resolvedClassMap.put(interningFeature.intern(name),
					new WeakReference<Class<?>>(type));
			return type;
		}
		finally {
			writeLock.unlock();
		}
	}

	private Class<?> getCachedClassEntry(String name, CacheEntry cacheEntry) {
		{
			Lock writeLock = cacheEntry.writeLock;
			writeLock.lock();
			try {
				Reference<Class<?>> typeR = cacheEntry.resolvedClassMap.get(name);
				return typeR != null ? typeR.get() : null;
			}
			finally {
				writeLock.unlock();
			}
		}
	}
}
