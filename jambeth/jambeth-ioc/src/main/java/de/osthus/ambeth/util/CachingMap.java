package de.osthus.ambeth.util;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.MapLinkedIterator;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.factory.BeanContextInitializer;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBeanExtendable;
import de.osthus.ambeth.util.CachingMap.LocalMap;

/**
 * Use SmartCopyMap instead
 */
@Deprecated
public class CachingMap<K, V> extends ThreadLocal<LocalMap<K, V>> implements ILinkedMap<K, V>, IThreadLocalCleanupBean, IInitializingBean, IStartingBean,
		IDisposableBean
{
	public static class LocalMap<K, V> extends LinkedHashMap<K, V>
	{
		protected int sharedModCount;
	}

	protected Map<K, V> sharedMap;

	// Intentionally not volatile
	protected int modCount = 0;

	protected IThreadLocalCleanupBeanExtendable threadLocalCleanupBeanExtendable;

	protected Lock writeLock;

	/**
	 * Bean inheritance mode
	 */
	protected CachingMap()
	{
		// Intended blank
	}

	/**
	 * Standalone mode
	 */
	public CachingMap(boolean weakKey)
	{
		IServiceContext beanContext = BeanContextInitializer.getCurrentBeanContext();
		initializeCachingMap(weakKey);
		IBeanContextFactory bcf = BeanContextInitializer.getCurrentBeanContextFactory();
		if (bcf != null && (beanContext == null || !beanContext.isRunning()))
		{
			bcf.link(this).to(IThreadLocalCleanupBeanExtendable.class);
		}
		else if (beanContext != null)
		{
			beanContext.link(this).to(IThreadLocalCleanupBeanExtendable.class);
		}
	}

	/**
	 * Standalone mode
	 */
	public CachingMap(boolean weakKey, Lock writeLock)
	{
		ParamChecker.assertParamNotNull(writeLock, "writeLock");
		this.writeLock = writeLock;
		IServiceContext beanContext = BeanContextInitializer.getCurrentBeanContext();
		initializeCachingMap(weakKey);
		IBeanContextFactory bcf = BeanContextInitializer.getCurrentBeanContextFactory();
		if (bcf != null && (beanContext == null || !beanContext.isRunning()))
		{
			bcf.link(this).to(IThreadLocalCleanupBeanExtendable.class);
		}
		else if (beanContext != null)
		{
			beanContext.link(this).to(IThreadLocalCleanupBeanExtendable.class);
		}
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(threadLocalCleanupBeanExtendable, "ThreadLocalCleanupBeanExtendable");
	}

	@Override
	public void afterStarted() throws Throwable
	{
		threadLocalCleanupBeanExtendable.registerThreadLocalCleanupBean(this);
	}

	@Override
	public void destroy() throws Throwable
	{
		threadLocalCleanupBeanExtendable.unregisterThreadLocalCleanupBean(this);
	}

	public void setThreadLocalCleanupBeanExtendable(IThreadLocalCleanupBeanExtendable threadLocalCleanupBeanExtendable)
	{
		this.threadLocalCleanupBeanExtendable = threadLocalCleanupBeanExtendable;
	}

	public void initializeCachingMap(boolean weakKey)
	{
		if (writeLock == null)
		{
			writeLock = createWriteLock();
		}
		if (weakKey)
		{
			sharedMap = new WeakHashMap<K, V>();
		}
		else
		{
			sharedMap = new LinkedHashMap<K, V>();
		}
	}

	protected Lock createWriteLock()
	{
		return new ReentrantLock();
	}

	@Override
	public void cleanupThreadLocal()
	{
		Map<K, V> localMap = get();
		if (localMap == null)
		{
			return;
		}
		remove();
	}

	public void increaseModCount()
	{
		modCount++;
		LocalMap<K, V> localMap = get();
		if (localMap != null)
		{
			localMap.sharedModCount = modCount;
		}
	}

	protected LocalMap<K, V> getLocalMap()
	{
		LocalMap<K, V> localMap = get();
		if (localMap == null)
		{
			localMap = new LocalMap<K, V>();
			localMap.sharedModCount = modCount;
			set(localMap);
		}
		else if (localMap.sharedModCount != modCount)
		{
			// If the modcount differs invalidate all TL cached information
			localMap.clear();
			localMap.sharedModCount = modCount;
		}
		return localMap;
	}

	protected LocalMap<K, V> getFullLocalMap()
	{
		LocalMap<K, V> localMap = getLocalMap();
		if (localMap.size() == sharedMap.size())
		{
			return localMap;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			if (localMap.size() > 0)
			{
				localMap.clear();
			}
			localMap.putAll(sharedMap);
			return localMap;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public ISet<Entry<K, V>> entrySet()
	{
		return getFullLocalMap().entrySet();
	}

	@Override
	public void entrySet(ISet<Entry<K, V>> targetEntrySet)
	{
		getFullLocalMap().entrySet(targetEntrySet);
	}

	@Override
	public ISet<K> keySet()
	{
		return getFullLocalMap().keySet();
	}

	@Override
	public void keySet(ISet<K> targetKeySet)
	{
		getFullLocalMap().keySet(targetKeySet);
	}

	@Override
	public IList<V> values()
	{
		return getFullLocalMap().values();
	}

	@Override
	public K getKey(K key)
	{
		return getLocalMap().getKey(key);
	}

	@Override
	public boolean putIfNotExists(K key, V value)
	{
		LocalMap<K, V> localMap = getLocalMap();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			V existingValue = sharedMap.get(key);
			if (existingValue != null || sharedMap.containsKey(key))
			{
				// Put shared entry to local cache. This is to provide a later local hit on get(key)
				localMap.put(key, existingValue);
				return false;
			}
			increaseModCount();
			sharedMap.put(key, value);
			localMap.put(key, value);
			return true;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public boolean removeIfValue(K key, V value)
	{
		if (!getFullLocalMap().removeIfValue(key, value))
		{
			return false;
		}
		remove(key);
		return true;
	}

	@Override
	public void clear()
	{
		Map<K, V> localMap = get();
		if (localMap != null)
		{
			localMap.clear();
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			increaseModCount();
			sharedMap.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key)
	{
		LocalMap<K, V> localMap = getLocalMap();
		if (localMap.containsKey(key))
		{
			return true;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			V value = sharedMap.get(key);
			if (value == null && !sharedMap.containsKey(key))
			{
				return false;
			}
			localMap.put((K) key, value);
			return true;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public boolean containsValue(Object value)
	{
		return getFullLocalMap().containsValue(value);
	}

	protected V cloneValue(V value)
	{
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key)
	{
		LocalMap<K, V> localMap = getLocalMap();
		V value = localMap.get(key);
		if (value != null || localMap.containsKey(key))
		{
			return value;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			value = sharedMap.get(key);
			if (value == null && !sharedMap.containsKey(key))
			{
				return null;
			}
			value = cloneValue(value);
		}
		finally
		{
			writeLock.unlock();
		}
		localMap.put((K) key, value);
		return value;
	}

	@Override
	public boolean isEmpty()
	{
		return getFullLocalMap().isEmpty();
	}

	@Override
	public V put(K key, V value)
	{
		LocalMap<K, V> localMap = getLocalMap();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			localMap.put(key, value);
			increaseModCount();
			return sharedMap.put(key, value);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		LocalMap<K, V> localMap = getLocalMap();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			localMap.putAll(m);
			increaseModCount();
			sharedMap.putAll(m);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public V remove(Object key)
	{
		Map<K, V> localMap = get();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			localMap.remove(key);
			if (!sharedMap.containsKey(key))
			{
				return null;
			}
			increaseModCount();
			return sharedMap.remove(key);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public MapLinkedIterator<K, V> iterator()
	{
		return getFullLocalMap().iterator();
	}

	@Override
	public MapLinkedIterator<K, V> iterator(boolean removeAllowed)
	{
		return getFullLocalMap().iterator(removeAllowed);
	}

	@Override
	public V[] toArray(Class<V> arrayType)
	{
		return getFullLocalMap().toArray(arrayType);
	}

	@Override
	public void toKeysList(List<K> list)
	{
		getFullLocalMap().toKeysList(list);
	}

	@Override
	public int size()
	{
		return getFullLocalMap().size();
	}
}
