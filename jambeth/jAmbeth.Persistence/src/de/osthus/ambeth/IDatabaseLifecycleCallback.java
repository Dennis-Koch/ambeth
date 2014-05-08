package de.osthus.ambeth;

import de.osthus.ambeth.persistence.IDatabase;

public interface IDatabaseLifecycleCallback
{

	void databaseNotFound(Object databaseHandle, String dbName);

	void databaseEmpty(Object databaseHandle);

	void databaseConnected(IDatabase database);

	void databaseActivated(IDatabase database);

	void databasePassivated(IDatabase database);

	void databaseClosed(IDatabase database);

}
