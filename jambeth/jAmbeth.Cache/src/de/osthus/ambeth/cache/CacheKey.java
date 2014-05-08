package de.osthus.ambeth.cache;

public class CacheKey
{
	protected Class<?> entityType;

	protected byte idNameIndex;

	protected Object id;

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	public byte getIdNameIndex()
	{
		return idNameIndex;
	}

	public void setIdNameIndex(byte idNameIndex)
	{
		this.idNameIndex = idNameIndex;
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
		return this.entityType.hashCode() ^ this.id.hashCode();
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
		return this.id.equals(other.getId()) && this.entityType.equals(other.getEntityType()) && this.idNameIndex == other.getIdNameIndex();
	}

	@Override
	public String toString()
	{
		return "CacheKey: " + this.entityType.getName() + "(" + this.idNameIndex + "," + this.id + ")";
	}
}
