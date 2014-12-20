package de.osthus.ambeth.util;

import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.IObjRefFactory;

public class AlreadyLoadedCache implements IAlreadyLoadedCache, IInitializingBean, IDisposableBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IObjRefFactory objRefFactory;

	protected final LinkedHashMap<IdTypeTuple, ILoadContainer> keyToObjectMap = new LinkedHashMap<IdTypeTuple, ILoadContainer>();

	protected final LinkedHashMap<IdTypeTuple, IObjRef> keyToRefMap = new LinkedHashMap<IdTypeTuple, IObjRef>();

	protected final java.util.concurrent.locks.Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public void destroy() throws Throwable
	{
		clear();
	}

	@Override
	public void clear()
	{
		writeLock.lock();
		try
		{
			keyToObjectMap.clear();
			keyToRefMap.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public int size()
	{
		writeLock.lock();
		try
		{
			return keyToRefMap.size();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public IAlreadyLoadedCache snapshot()
	{
		// Intentionally a POJO
		AlreadyLoadedCache targetAlCache = new AlreadyLoadedCache();
		targetAlCache.log = log;
		copyTo(targetAlCache);
		return targetAlCache;
	}

	@Override
	public void copyTo(IAlreadyLoadedCache targetAlCache)
	{
		writeLock.lock();
		try
		{
			AlreadyLoadedCache realTargetAlCache = (AlreadyLoadedCache) targetAlCache;
			LinkedHashMap<IdTypeTuple, ILoadContainer> realKeyToObjectMap = realTargetAlCache.keyToObjectMap;
			LinkedHashMap<IdTypeTuple, IObjRef> realKeyToRefMap = realTargetAlCache.keyToRefMap;
			for (Entry<IdTypeTuple, ILoadContainer> entry : keyToObjectMap)
			{
				if (!realKeyToObjectMap.putIfNotExists(entry.getKey(), entry.getValue()))
				{
					throw new IllegalStateException("LoadContainer already in map. This must never happen - Parallel EntityLoader still buggy?");
				}
			}
			for (Entry<IdTypeTuple, IObjRef> entry : keyToRefMap)
			{
				if (!realKeyToRefMap.putIfNotExists(entry.getKey(), entry.getValue()))
				{
					if (log.isWarnEnabled())
					{
						log.warn("ObjRef " + entry.getKey() + " already instantiated. This may be a bug and should be further analyzed");
					}
				}
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public ILoadContainer getObject(byte idNameIndex, Object id, Class<?> type)
	{
		return getObject(new IdTypeTuple(type, idNameIndex, id));
	}

	@Override
	public ILoadContainer getObject(IdTypeTuple idTypeTuple)
	{
		writeLock.lock();
		try
		{
			return keyToObjectMap.get(idTypeTuple);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public IObjRef getRef(byte idNameIndex, Object id, Class<?> type)
	{
		return getRef(new IdTypeTuple(type, idNameIndex, id));
	}

	@Override
	public IObjRef getRef(IdTypeTuple idTypeTuple)
	{
		writeLock.lock();
		try
		{
			return keyToRefMap.get(idTypeTuple);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void add(byte idNameIndex, Object id, Class<?> type, IObjRef objRef)
	{
		add(new IdTypeTuple(type, idNameIndex, id), objRef);
	}

	@Override
	public void add(IdTypeTuple idTypeTuple, IObjRef objRef)
	{
		writeLock.lock();
		try
		{
			if (!keyToRefMap.putIfNotExists(idTypeTuple, objRef))
			{
				throw new RuntimeException();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void add(byte idNameIndex, Object persistentId, Class<?> type, IObjRef objRef, ILoadContainer loadContainer)
	{
		add(new IdTypeTuple(type, idNameIndex, persistentId), objRef, loadContainer);
	}

	@Override
	public void add(IdTypeTuple idTypeTuple, IObjRef objRef, ILoadContainer loadContainer)
	{
		writeLock.lock();
		try
		{
			keyToRefMap.putIfNotExists(idTypeTuple, objRef);
			keyToObjectMap.put(idTypeTuple, loadContainer);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void replace(byte idNameIndex, Object persistentId, Class<?> type, ILoadContainer loadContainer)
	{
		replace(new IdTypeTuple(type, idNameIndex, persistentId), loadContainer);
	}

	@Override
	public void replace(IdTypeTuple idTypeTuple, ILoadContainer loadContainer)
	{
		IObjRef objRef = objRefFactory.createObjRef(idTypeTuple.type, idTypeTuple.idNameIndex, idTypeTuple.persistentId, null);
		writeLock.lock();
		try
		{
			keyToRefMap.putIfNotExists(idTypeTuple, objRef);
			keyToObjectMap.put(idTypeTuple, loadContainer);
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
