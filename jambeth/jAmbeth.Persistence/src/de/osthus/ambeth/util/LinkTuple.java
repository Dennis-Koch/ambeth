package de.osthus.ambeth.util;

public class LinkTuple
{

	public Object leftRecId, rightRecId;

	@Override
	public int hashCode()
	{
		return this.leftRecId.hashCode() ^ this.rightRecId.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof LinkTuple))
		{
			return false;
		}
		LinkTuple other = (LinkTuple) obj;
		return leftRecId.equals(other.leftRecId) && rightRecId.equals(other.rightRecId);
	}

}
