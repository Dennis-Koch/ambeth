package de.osthus.ambeth.database;

import de.osthus.ambeth.IDatabasePool;
import de.osthus.ambeth.persistence.IDatabase;

public interface IDatabaseFactory
{
	IDatabase createDatabaseInstance(IDatabasePool pool);

	void activate(IDatabase database);

	void passivate(IDatabase database);
}
