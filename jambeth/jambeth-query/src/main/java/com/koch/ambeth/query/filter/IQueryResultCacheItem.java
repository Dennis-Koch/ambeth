package com.koch.ambeth.query.filter;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface IQueryResultCacheItem {
	int getSize();

	long getTotalSize();

	IObjRef getObjRef(int index, byte idIndex);
}
