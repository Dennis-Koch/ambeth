package de.osthus.ambeth.database;

import de.osthus.ambeth.persistence.IDatabase;

public interface IDatabaseProvider
{
	ThreadLocal<IDatabase> getDatabaseLocal();

	IDatabase tryGetInstance();

	IDatabase acquireInstance();

	IDatabase acquireInstance(boolean readOnly);
}