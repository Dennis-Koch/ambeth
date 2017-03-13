package com.koch.ambeth.persistence.api.database;

import com.koch.ambeth.persistence.api.IDatabase;

public interface IDatabaseProvider
{
	ThreadLocal<IDatabase> getDatabaseLocal();

	IDatabase tryGetInstance();

	IDatabase acquireInstance();

	IDatabase acquireInstance(boolean readOnly);
}