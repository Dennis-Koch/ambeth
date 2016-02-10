package de.osthus.ambeth.database;

import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.ITableMetaData;

public interface IDatabaseMappedListener
{
	void databaseMapped(IDatabaseMetaData database);

	void newTableMetaData(ITableMetaData newTable);
}
