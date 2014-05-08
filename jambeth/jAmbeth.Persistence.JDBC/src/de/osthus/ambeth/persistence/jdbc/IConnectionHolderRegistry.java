package de.osthus.ambeth.persistence.jdbc;

import de.osthus.ambeth.collections.ILinkedMap;

public interface IConnectionHolderRegistry
{
	ILinkedMap<Object, IConnectionHolder> getPersistenceUnitToConnectionHolderMap();
}