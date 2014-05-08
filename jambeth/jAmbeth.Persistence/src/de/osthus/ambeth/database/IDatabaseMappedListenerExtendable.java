package de.osthus.ambeth.database;

public interface IDatabaseMappedListenerExtendable
{
	void registerDatabaseMappedListener(IDatabaseMappedListener databaseMappedListener);

	void unregisterDatabaseMappedListener(IDatabaseMappedListener databaseMappedListener);
}
