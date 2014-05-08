package de.osthus.ambeth.filter;

import java.lang.reflect.Array;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;

public class QueryResultCacheItem implements IQueryResultCacheItem
{
	protected final Class<?> entityType;

	protected final int size;

	protected final Object[] idArrays;

	protected final Object versionArray;

	public QueryResultCacheItem(Class<?> entityType, int size, Object[] idArrays, Object versionArray)
	{
		super();
		this.entityType = entityType;
		this.size = size;
		this.idArrays = idArrays;
		this.versionArray = versionArray;
	}

	@Override
	public int getSize()
	{
		return size;
	}

	@Override
	public IObjRef getObjRef(int index, byte idIndex)
	{
		Object id = Array.get(idArrays[idIndex + 1], index);
		Object version = versionArray != null ? Array.get(versionArray, index) : null;
		return new ObjRef(entityType, idIndex, id, version);
	}
}
