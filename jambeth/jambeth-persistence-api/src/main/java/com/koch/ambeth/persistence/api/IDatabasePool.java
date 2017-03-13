package com.koch.ambeth.persistence.api;


public interface IDatabasePool
{
	IDatabase acquireDatabase();

	IDatabase acquireDatabase(boolean readonlyMode);

	IDatabase tryAcquireDatabase();

	IDatabase tryAcquireDatabase(boolean readonlyMode);

	void releaseDatabase(IDatabase database);

	void releaseDatabase(IDatabase database, boolean backToPool);

	void shutdown();
}
