package de.osthus.ambeth.cache;

public class CacheKey
{
	protected Class<?> entityType;

	protected byte idIndex;

	protected Object id;

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	public byte getIdIndex()
	{
		return idIndex;
	}

	public void setIdIndex(byte idIndex)
	{
		this.idIndex = idIndex;
	}

	public Object getId()
	{
		return id;
	}

	public void setId(Object id)
	{
		this.id = id;
	}

	@Override
	public int hashCode()
	{
		return entityType.hashCode() ^ id.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof CacheKey))
		{
			return false;
		}
		CacheKey other = (CacheKey) obj;
		return id.equals(other.getId()) && entityType.equals(other.getEntityType()) && idIndex == other.getIdIndex();
	}

	@Override
	public String toString()
	{
		return "CacheKey: " + entityType.getName() + "(" + idIndex + "," + id + ")";
	}
}
