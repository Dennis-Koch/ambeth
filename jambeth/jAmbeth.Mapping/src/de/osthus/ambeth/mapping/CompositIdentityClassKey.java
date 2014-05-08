package de.osthus.ambeth.mapping;

public class CompositIdentityClassKey
{
	private final Object entity;

	private final Class<?> type;

	private int hash;

	public CompositIdentityClassKey(Object entity, Class<?> type)
	{
		this.entity = entity;
		this.type = type;

		hash = System.identityHashCode(entity) * 13;
		if (type != null)
		{
			hash += type.hashCode() * 23;
		}
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof CompositIdentityClassKey))
		{
			return false;
		}
		CompositIdentityClassKey otherKey = (CompositIdentityClassKey) other;
		boolean ee = entity == otherKey.entity;
		boolean te = type == otherKey.type;
		return ee && te;
	}

	@Override
	public int hashCode()
	{
		return hash;
	}
}
