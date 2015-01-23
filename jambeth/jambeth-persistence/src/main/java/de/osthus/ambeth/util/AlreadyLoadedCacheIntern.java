package de.osthus.ambeth.util;

import java.util.Map.Entry;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.collections.ChildHashMap;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.IObjRefFactory;

public class AlreadyLoadedCacheIntern implements IAlreadyLoadedCache
{
	private final ILogger log;

	private final IObjRefFactory objRefFactory;

	private final HashMap<IdTypeTuple, ILoadContainer> keyToObjectMap;

	private final HashMap<IdTypeTuple, IObjRef> keyToRefMap;

	public AlreadyLoadedCacheIntern(ILogger log, IObjRefFactory objRefFactory)
	{
		this.log = log;
		this.objRefFactory = objRefFactory;
		keyToObjectMap = new HashMap<IdTypeTuple, ILoadContainer>();
		keyToRefMap = new HashMap<IdTypeTuple, IObjRef>();
	}

	private AlreadyLoadedCacheIntern(AlreadyLoadedCacheIntern parent)
	{
		log = parent.log;
		objRefFactory = parent.objRefFactory;
		keyToObjectMap = new ChildHashMap<IdTypeTuple, ILoadContainer>(parent.keyToObjectMap);
		keyToRefMap = new ChildHashMap<IdTypeTuple, IObjRef>(parent.keyToRefMap);
	}

	public AlreadyLoadedCacheIntern createChild()
	{
		return new AlreadyLoadedCacheIntern(this);
	}

	public void applyContentFrom(AlreadyLoadedCacheIntern forkedValue)
	{
		for (Entry<IdTypeTuple, ILoadContainer> entry : forkedValue.keyToObjectMap)
		{
			IdTypeTuple key = entry.getKey();
			ILoadContainer loadContainer = entry.getValue();
			ILoadContainer existingLoadContainer = keyToObjectMap.get(key);
			if (existingLoadContainer == null)
			{
				keyToObjectMap.put(key, loadContainer);
				continue;
			}
		}
		for (Entry<IdTypeTuple, IObjRef> entry : forkedValue.keyToRefMap)
		{
			IdTypeTuple key = entry.getKey();
			IObjRef objRef = entry.getValue();
			IObjRef existingObjRef = keyToRefMap.get(key);
			if (existingObjRef == null)
			{
				keyToRefMap.put(key, objRef);
				continue;
			}
		}
	}

	@Override
	public IAlreadyLoadedCache getCurrent()
	{
		return this;
	}

	@Override
	public void clear()
	{
		keyToObjectMap.clear();
		keyToRefMap.clear();
	}

	@Override
	public int size()
	{
		return keyToRefMap.size();
	}

	@Override
	public IAlreadyLoadedCache snapshot()
	{
		return createChild();
	}

	@Override
	public void copyTo(IAlreadyLoadedCache targetAlCache)
	{
		AlreadyLoadedCacheIntern realTargetAlCache = (AlreadyLoadedCacheIntern) targetAlCache;
		HashMap<IdTypeTuple, ILoadContainer> realKeyToObjectMap = realTargetAlCache.keyToObjectMap;
		HashMap<IdTypeTuple, IObjRef> realKeyToRefMap = realTargetAlCache.keyToRefMap;
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
		return keyToRefMap.get(idTypeTuple);
	}

	@Override
	public void add(byte idNameIndex, Object id, Class<?> type, IObjRef objRef)
	{
		add(new IdTypeTuple(type, idNameIndex, id), objRef);
	}

	@Override
	public void add(IdTypeTuple idTypeTuple, IObjRef objRef)
	{
		if (!keyToRefMap.putIfNotExists(idTypeTuple, objRef))
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
		keyToRefMap.putIfNotExists(idTypeTuple, objRef);
		keyToObjectMap.put(idTypeTuple, loadContainer);
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
		keyToRefMap.putIfNotExists(idTypeTuple, objRef);
		keyToObjectMap.put(idTypeTuple, loadContainer);
	}
}
