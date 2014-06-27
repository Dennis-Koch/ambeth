package de.osthus.ambeth.database;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.persistence.IDatabase;

public interface DatabaseCallback
{
	void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable;
}
