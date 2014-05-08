package de.osthus.ambeth.xml;

import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.HandleContentDelegate;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.Lock;

public class CacheDummy implements ICache
{
	@LogInstance
	private ILogger log;

	@Override
	public <E> E getObject(Class<E> type, Object... compositeIdParts)
	{
		return null;
	}

	@Override
	public <E> IList<E> getObjects(Class<E> type, Object... ids)
	{
		return null;
	}

	@Override
	public <E> IList<E> getObjects(Class<E> type, List<?> ids)
	{
		return null;
	}

	@Override
	public IList<Object> getObjects(IObjRef[] orisToGetArray, Set<CacheDirective> cacheDirective)
	{
		return null;
	}

	@Override
	public IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective)
	{
		return null;
	}

	@Override
	public IList<IObjRelationResult> getObjRelations(List<IObjRelation> self, Set<CacheDirective> cacheDirective)
	{
		return null;
	}

	@Override
	public Object getObject(IObjRef oriToGet, Set<CacheDirective> cacheDirective)
	{
		return null;
	}

	@Override
	public <E> E getObject(Class<E> type, Object id)
	{
		return null;
	}

	@Override
	public <E> E getObject(Class<E> type, String idName, Object id)
	{
		return null;
	}

	@Override
	public <E> E getObject(Class<E> type, Object id, Set<CacheDirective> cacheDirective)
	{
		return null;
	}

	@Override
	public <E> E getObject(Class<E> type, String idName, Object id, Set<CacheDirective> cacheDirective)
	{
		return null;
	}

	@Override
	public void getContent(HandleContentDelegate handleContentDelegate)
	{
	}

	@Override
	public Lock getReadLock()
	{
		return null;
	}

	@Override
	public Lock getWriteLock()
	{
		return null;
	}

	@Override
	public void cascadeLoadPath(Class<?> entityType, String cascadeLoadPath)
	{
		throw new UnsupportedOperationException("Not implemented");
	}
}
