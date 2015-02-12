package de.osthus.ambeth.database;

import de.osthus.ambeth.persistence.IDatabaseMetaData;

public interface IDatabaseMappedListener
{
	void databaseMapped(IDatabaseMetaData database);
}
