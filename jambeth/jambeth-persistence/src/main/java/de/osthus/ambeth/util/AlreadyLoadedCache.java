package de.osthus.ambeth.util;

import java.util.Map.Entry;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;

public class AlreadyLoadedCache implements IAlreadyLoadedCache, IInitializingBean, IDisposableBean
{
	@LogInstance
	private ILogger log;

	protected final LinkedHashMap<IdTypeTuple, ILoadContainer> keyToObjectMap = new LinkedHashMap<IdTypeTuple, ILoadContainer>();

	protected final LinkedHashMap<IdTypeTuple, IObjRef> keyToRefMap = new LinkedHashMap<IdTypeTuple, IObjRef>();

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
		this.keyToObjectMap.clear();
		this.keyToRefMap.clear();
	}

	@Override
	public int size()
	{
		return this.keyToRefMap.size();
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

	@Override
	public ILoadContainer getObject(byte idNameIndex, Object id, Class<?> type)
	{
		return getObject(new IdTypeTuple(type, idNameIndex, id));
	}

	@Override
	public ILoadContainer getObject(IdTypeTuple idTypeTuple)
	{
		return keyToObjectMap.get(idTypeTuple);
	}

	@Override
	public IObjRef getRef(byte idNameIndex, Object id, Class<?> type)
	{
		return getRef(new IdTypeTuple(type, idNameIndex, id));
	}

	@Override
	public IObjRef getRef(IdTypeTuple idTypeTuple)
	{
		return this.keyToRefMap.get(idTypeTuple);
	}

	@Override
	public void add(byte idNameIndex, Object id, Class<?> type, IObjRef objRef)
	{
		add(new IdTypeTuple(type, idNameIndex, id), objRef);
	}

	@Override
	public void add(IdTypeTuple idTypeTuple, IObjRef objRef)
	{
		if (!this.keyToRefMap.putIfNotExists(idTypeTuple, objRef))
		{
			throw new RuntimeException();
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
		this.keyToRefMap.putIfNotExists(idTypeTuple, objRef);
		this.keyToObjectMap.put(idTypeTuple, loadContainer);
	}

	@Override
	public void replace(byte idNameIndex, Object persistentId, Class<?> type, ILoadContainer loadContainer)
	{
		replace(new IdTypeTuple(type, idNameIndex, persistentId), loadContainer);
	}

	@Override
	public void replace(IdTypeTuple idTypeTuple, ILoadContainer loadContainer)
	{
		this.keyToRefMap.putIfNotExists(idTypeTuple, new ObjRef(idTypeTuple.type, idTypeTuple.idNameIndex, idTypeTuple.persistentId, null));
		this.keyToObjectMap.put(idTypeTuple, loadContainer);
	}
}
