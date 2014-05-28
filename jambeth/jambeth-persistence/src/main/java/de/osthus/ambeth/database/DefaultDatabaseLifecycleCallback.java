package de.osthus.ambeth.database;

import de.osthus.ambeth.IDatabaseLifecycleCallback;
import de.osthus.ambeth.persistence.IDatabase;

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
