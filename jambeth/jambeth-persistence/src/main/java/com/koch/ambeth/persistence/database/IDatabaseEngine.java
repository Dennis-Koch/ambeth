package com.koch.ambeth.persistence.database;

import com.koch.ambeth.persistence.api.IDatabase;

public interface IDatabaseEngine
{

	void init(String host, int port, String databaseName);

	void init(IDatabaseFactory databaseFactory, String host, int port, String databaseName);

	void init(String databaseName);

	void init(IDatabaseFactory databaseFactory, String databaseName);

	void init(String host, int port, String databaseName, boolean localMode);

	void init(IDatabaseFactory databaseFactory, String host, int port, String databaseName, boolean localMode);

	IDatabase acquireDatabase(DatabaseType databaseType);

}
