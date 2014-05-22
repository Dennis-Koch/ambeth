package de.osthus.ambeth.util;

public class IdTypeTuple
{
	public final byte idNameIndex;

	public final Object id;

	public final Class<?> type;

	public IdTypeTuple(Class<?> type, byte idNameIndex, Object id)
	{
		this.type = type;
		this.idNameIndex = idNameIndex;
		this.id = id;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode() ^ type.hashCode();
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
		if (id instanceof Comparable && id.getClass().equals(other.id.getClass()))
		{
			idEqual = ((Comparable<Object>) id).compareTo(other.id) == 0;
		}
		else
		{
			idEqual = id.equals(other.id);
		}
		return idEqual && type.equals(other.type) && idNameIndex == other.idNameIndex;
	}

}
