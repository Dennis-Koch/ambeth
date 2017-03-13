package com.koch.ambeth.persistence.database;

import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.util.collections.ILinkedMap;

public interface IDatabaseProviderRegistry
{
	ILinkedMap<Object, IDatabaseProvider> getPersistenceUnitToDatabaseProviderMap();
}