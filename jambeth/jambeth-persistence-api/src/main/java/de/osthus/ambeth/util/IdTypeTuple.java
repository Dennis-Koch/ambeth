package de.osthus.ambeth.util;

public class IdTypeTuple
{
	public final byte idNameIndex;

	public final Object persistentId;

	public final Class<?> type;

	public IdTypeTuple(Class<?> type, byte idNameIndex, Object persistentId)
	{
		this.type = type;
		this.idNameIndex = idNameIndex;
		this.persistentId = persistentId;
	}

	@Override
	public int hashCode()
	{
		return persistentId.hashCode() ^ type.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof IdTypeTuple))
		{
			return false;
		}
		IdTypeTuple other = (IdTypeTuple) obj;
		boolean idEqual;
		if (persistentId instanceof Comparable && persistentId.getClass().equals(other.persistentId.getClass()))
		{
			idEqual = ((Comparable<Object>) persistentId).compareTo(other.persistentId) == 0;
		}
		else
		{
			idEqual = persistentId.equals(other.persistentId);
		}
		return idEqual && type.equals(other.type) && idNameIndex == other.idNameIndex;
	}

}
