package de.osthus.ambeth.service;

import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.ITable;

public class OutgoingRelationKey
{
	protected final byte idIndex;

	protected final ITable table;

	protected final IDirectedLink link;

	public OutgoingRelationKey(byte idIndex, ITable table, IDirectedLink link)
	{
		this.idIndex = idIndex;
		this.table = table;
		this.link = link;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof OutgoingRelationKey))
		{
			return false;
		}
		OutgoingRelationKey other = (OutgoingRelationKey) obj;
		return idIndex == other.idIndex && table.equals(other.table) && link.equals(other.link);
	}

	@Override
	public int hashCode()
	{
		return idIndex ^ table.hashCode() ^ link.hashCode();
	}

	@Override
	public String toString()
	{
		return idIndex + " " + table.getName() + " - " + link.getName();
	}
}
