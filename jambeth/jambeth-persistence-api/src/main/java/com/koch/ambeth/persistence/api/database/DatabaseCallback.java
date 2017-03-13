package com.koch.ambeth.persistence.api.database;

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.util.collections.ILinkedMap;

public interface DatabaseCallback
{
	void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable;
}
