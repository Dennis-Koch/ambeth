package com.koch.ambeth.persistence.filter;

import java.lang.reflect.Array;

import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.query.filter.IQueryResultCacheItem;
import com.koch.ambeth.service.merge.model.IObjRef;

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
