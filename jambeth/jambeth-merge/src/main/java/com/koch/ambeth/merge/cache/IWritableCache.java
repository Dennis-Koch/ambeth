package com.koch.ambeth.merge.cache;

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface IWritableCache extends ICache
{
	int getCacheId();

	void clear();

	void setCacheId(int cacheId);

	List<Object> put(Object objectToCache);

	void remove(List<IObjRef> oris);

	void remove(IObjRef ori);

	void remove(Class<?> type, Object id);

	void removePriorVersions(List<IObjRef> oris);

	void removePriorVersions(IObjRef ori);
}
