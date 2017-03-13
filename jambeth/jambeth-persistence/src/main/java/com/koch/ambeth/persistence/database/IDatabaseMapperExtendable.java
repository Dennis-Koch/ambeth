package com.koch.ambeth.persistence.database;

public interface IDatabaseMapperExtendable
{

	void registerDatabaseMapper(IDatabaseMapper databaseMapper);

	void unregisterDatabaseMapper(IDatabaseMapper databaseMapper);

}
