package de.osthus.ambeth.util;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IAlreadyLoadedCache
{
	IAlreadyLoadedCache getCurrent();

	void clear();

	int size();

	IAlreadyLoadedCache snapshot();

	void copyTo(IAlreadyLoadedCache targetAlCache);

	ILoadContainer getObject(byte idNameIndex, Object id, Class<?> type);

	ILoadContainer getObject(IdTypeTuple idTypeTuple);

	IObjRef getRef(byte idNameIndex, Object id, Class<?> type);

	IObjRef getRef(IdTypeTuple idTypeTuple);

	void add(byte idNameIndex, Object id, Class<?> type, IObjRef objRef);

	void add(IdTypeTuple idTypeTuple, IObjRef objRef);

	void add(byte idNameIndex, Object persistentId, Class<?> type, IObjRef objRef, ILoadContainer loadContainer);

	void add(IdTypeTuple idTypeTuple, IObjRef objRef, ILoadContainer loadContainer);

	void replace(byte idNameIndex, Object persistentId, Class<?> type, ILoadContainer loadContainer);

	void replace(IdTypeTuple idTypeTuple, ILoadContainer loadContainer);

}
