package com.koch.ambeth.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class CacheSoftReference<V> extends SoftReference<V> implements ICacheReference, IParentCacheValueHardRef
{
	@SuppressWarnings("unused")
	private Object parentCacheValueHardRef;

	private final Class<?> entityType;

	private final byte idIndex;

	private final Object id;

	public CacheSoftReference(V referent, ReferenceQueue<V> queue, Class<?> entityType, byte idIndex, Object id)
	{
		super(referent, queue);
		this.entityType = entityType;
		this.idIndex = idIndex;
		this.id = id;
	}

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	@Override
	public byte getIdIndex()
	{
		return idIndex;
	}

	@Override
	public Object getId()
	{
		return id;
	}

	@Override
	public void setParentCacheValueHardRef(Object parentCacheValueHardRef)
	{
		this.parentCacheValueHardRef = parentCacheValueHardRef;
	}
}
