package de.osthus.ambeth.database;

import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDatabasePool;

public interface IDatabaseFactory
{
	IDatabase createDatabaseInstance(IDatabasePool pool);

	void activate(IDatabase database);

	void passivate(IDatabase database);
}
