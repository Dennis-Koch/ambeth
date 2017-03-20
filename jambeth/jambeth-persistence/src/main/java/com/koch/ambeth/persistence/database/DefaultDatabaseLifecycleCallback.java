package com.koch.ambeth.persistence.database;

import com.koch.ambeth.persistence.api.IDatabase;

public abstract class DefaultDatabaseLifecycleCallback implements IDatabaseLifecycleCallback
{

	@Override
	public void databaseNotFound(Object databaseHandle, String dbName)
	{
		// Intended blank
	}

	@Override
	public void databaseEmpty(Object databaseHandle)
	{
		// Intended blank
	}

	@Override
	public void databaseConnected(IDatabase database)
	{
		// Intended blank
	}

	@Override
	public void databaseClosed(IDatabase database)
	{
		// Intended blank
	}

}
