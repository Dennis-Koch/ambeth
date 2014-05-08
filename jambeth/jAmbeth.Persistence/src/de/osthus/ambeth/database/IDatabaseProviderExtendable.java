package de.osthus.ambeth.database;

public interface IDatabaseProviderExtendable
{
	void registerDatabaseProvider(IDatabaseProvider databaseProvider, Object persistenceUnitId);

	void unregisterDatabaseProvider(IDatabaseProvider databaseProvider, Object persistenceUnitId);
}
