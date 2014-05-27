package de.osthus.ambeth.filter;

import de.osthus.ambeth.merge.model.IObjRef;

public interface IQueryResultCacheItem
{
	int getSize();

	IObjRef getObjRef(int index, byte idIndex);
}