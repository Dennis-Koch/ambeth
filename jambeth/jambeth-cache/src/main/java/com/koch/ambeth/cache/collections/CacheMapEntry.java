package com.koch.ambeth.cache.collections;

public abstract class CacheMapEntry
{
	private CacheMapEntry nextEntry;

	private Object value;

	public CacheMapEntry(Class<?> entityType, byte idIndex, Object id, Object value, CacheMapEntry nextEntry)
	{
		setId(id);
		this.value = value;
		this.nextEntry = nextEntry;
	}

	protected abstract void setId(Object id);

	public abstract Object getId();

	public abstract Class<?> getEntityType();

	public abstract byte getIdIndex();

	public boolean isEqualTo(Class<?> entityType, byte idIndex, Object id)
	{
		return getId().equals(id) && getEntityType().equals(entityType) && getIdIndex() == idIndex;
	}

	public CacheMapEntry getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(CacheMapEntry nextEntry)
	{
		this.nextEntry = nextEntry;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public Object getValue()
	{
		return value;
	}
}
