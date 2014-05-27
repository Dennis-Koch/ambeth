package de.osthus.ambeth.persistence.jdbc.database;

import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.persistence.IDatabase;

public class TransactionBeginEvent
{
	protected final LinkedHashMap<Object, IDatabase> persistenceUnitToDatabaseMap;

	public TransactionBeginEvent(LinkedHashMap<Object, IDatabase> persistenceUnitToDatabaseMap)
	{
		this.persistenceUnitToDatabaseMap = persistenceUnitToDatabaseMap;
	}

	public LinkedHashMap<Object, IDatabase> getPersistenceUnitToDatabaseMap()
	{
		return persistenceUnitToDatabaseMap;
	}
}
