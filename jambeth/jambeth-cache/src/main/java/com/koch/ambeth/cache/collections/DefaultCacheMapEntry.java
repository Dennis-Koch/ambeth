package com.koch.ambeth.cache.collections;

public class DefaultCacheMapEntry extends CacheMapEntry
{
	private final Class<?> entityType;
	private final byte idIndex;
	private Object id;

	public DefaultCacheMapEntry(Class<?> entityType, byte idIndex, Object id, Object value, CacheMapEntry nextEntry)
	{
		super(entityType, idIndex, id, value, nextEntry);
		this.entityType = entityType;
		this.idIndex = idIndex;
	}

	@Override
	public Object getId()
	{
		return id;
	}

	@Override
	protected void setId(Object id)
	{
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
}
