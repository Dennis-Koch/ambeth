package com.koch.ambeth.persistence.database;

public interface IDatabaseMappedListenerExtendable
{
	void registerDatabaseMappedListener(IDatabaseMappedListener databaseMappedListener);

	void unregisterDatabaseMappedListener(IDatabaseMappedListener databaseMappedListener);
}
