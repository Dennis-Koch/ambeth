package de.osthus.ambeth.database;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.persistence.IDatabase;

public interface ResultingDatabaseCallback<R>
{
	R callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception;
}
