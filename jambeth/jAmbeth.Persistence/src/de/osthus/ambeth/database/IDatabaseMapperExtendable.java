package de.osthus.ambeth.database;

public interface IDatabaseMapperExtendable
{

	void registerDatabaseMapper(IDatabaseMapper databaseMapper);

	void unregisterDatabaseMapper(IDatabaseMapper databaseMapper);

}
