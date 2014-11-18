package de.osthus.esmeralda;

public class TypeUsing implements Comparable<TypeUsing>
{
	protected final String typeName;

	protected boolean inSilverlightOnly;

	public TypeUsing(String typeName, boolean inSilverlightOnly)
	{
		this.typeName = typeName;
		this.inSilverlightOnly = inSilverlightOnly;
	}

	public String getTypeName()
	{
		return typeName;
	}

	public boolean isInSilverlightOnly()
	{
		return inSilverlightOnly;
	}

	@Override
	public int compareTo(TypeUsing o)
	{
		if (!(isInSilverlightOnly() ^ o.isInSilverlightOnly()))
		{
			// if both flags are equal sort by typeName
			return getTypeName().compareTo(o.getTypeName());
		}
		if (isInSilverlightOnly())
		{
			// silverlight flags always after non-flags
			return 1;
		}
		return -1;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof TypeUsing))
		{
			return false;
		}
		TypeUsing other = (TypeUsing) obj;
		return typeName.equals(other.typeName);
	}

	@Override
	public int hashCode()
	{
		return typeName.hashCode();
	}
}
