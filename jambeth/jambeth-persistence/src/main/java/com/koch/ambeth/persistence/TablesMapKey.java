package com.koch.ambeth.persistence;

import com.koch.ambeth.persistence.api.ITableMetaData;

public class TablesMapKey
{
	private ITableMetaData table1, table2;

	public TablesMapKey(ITableMetaData table1, ITableMetaData table2)
	{
		this.table1 = table1;
		this.table2 = table2;
	}

	@Override
	public int hashCode()
	{
		return table1.hashCode() ^ table2.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof TablesMapKey))
		{
			return false;
		}
		return equals((TablesMapKey) obj);
	}

	public boolean equals(TablesMapKey other)
	{
		return (table1.equals(other.table1) && table2.equals(other.table2)) || (table1.equals(other.table2) && table2.equals(other.table1));
	}
}
