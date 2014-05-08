package de.osthus.ambeth.database;

import de.osthus.ambeth.collections.ILinkedMap;

public interface IDatabaseProviderRegistry
{
	ILinkedMap<Object, IDatabaseProvider> getPersistenceUnitToDatabaseProviderMap();
}