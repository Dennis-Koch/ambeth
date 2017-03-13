package com.koch.ambeth.persistence.database;

import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;

public interface IDatabaseMappedListener
{
	void databaseMapped(IDatabaseMetaData database);

	void newTableMetaData(ITableMetaData newTable);
}
