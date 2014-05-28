package de.osthus.ambeth.sql;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.persistence.ITable;

public interface IPrimaryKeyProvider
{
	IList<Object> acquireIds(ITable table, int count);
}
