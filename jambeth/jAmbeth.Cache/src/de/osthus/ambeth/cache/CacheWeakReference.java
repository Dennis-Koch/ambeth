package de.osthus.ambeth.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import de.osthus.ambeth.cache.collections.CacheMapEntry;
import de.osthus.ambeth.cache.collections.ICacheMapEntryAware;

public class CacheWeakReference<V> extends WeakReference<V> implements ICacheReference, IParentCacheValueHardRef, ICacheMapEntryAware
{
	@SuppressWarnings("unused")
	private Object parentCacheValueHardRef;

	private CacheMapEntry cacheMapEntry;

	public CacheWeakReference(V referent, ReferenceQueue<V> queue)
	{
		super(referent, queue);
	}

	@Override
	public void setCacheMapEntry(CacheMapEntry cacheMapEntry)
	{
		this.cacheMapEntry = cacheMapEntry;
	}

	@Override
	public Class<?> getEntityType()
	{
		return cacheMapEntry.getEntityType();
	}

	@Override
	public byte getIdIndex()
	{
		return cacheMapEntry.getIdIndex();
	}

	@Override
	public Object getId()
	{
		return cacheMapEntry.getId();
	}

	@Override
	public void setParentCacheValueHardRef(Object parentCacheValueHardRef)
	{
		this.parentCacheValueHardRef = parentCacheValueHardRef;
	}
}
