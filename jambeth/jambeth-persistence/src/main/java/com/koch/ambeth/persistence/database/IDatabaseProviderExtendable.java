package com.koch.ambeth.persistence.database;

import com.koch.ambeth.persistence.api.database.IDatabaseProvider;

public interface IDatabaseProviderExtendable
{
	void registerDatabaseProvider(IDatabaseProvider databaseProvider, Object persistenceUnitId);

	void unregisterDatabaseProvider(IDatabaseProvider databaseProvider, Object persistenceUnitId);
}
