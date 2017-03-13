package com.koch.ambeth.query.filter;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface IQueryResultCacheItem
{
	int getSize();

	IObjRef getObjRef(int index, byte idIndex);
}