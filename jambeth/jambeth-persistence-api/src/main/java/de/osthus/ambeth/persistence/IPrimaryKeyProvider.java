package de.osthus.ambeth.persistence;

import de.osthus.ambeth.collections.IList;

public interface IPrimaryKeyProvider
{
	IList<Object> acquireIds(ITableMetaData table, int count);
}
