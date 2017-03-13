package com.koch.ambeth.persistence.database;

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabasePool;

public interface IDatabaseFactory
{
	IDatabase createDatabaseInstance(IDatabasePool pool);

	void activate(IDatabase database);

	void passivate(IDatabase database);
}
