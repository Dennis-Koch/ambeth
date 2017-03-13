package com.koch.ambeth.persistence.jdbc.database;

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.util.collections.LinkedHashMap;

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
