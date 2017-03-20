package com.koch.ambeth.persistence.jdbc;

import com.koch.ambeth.persistence.IConnectionHolder;
import com.koch.ambeth.util.collections.ILinkedMap;

public interface IConnectionHolderRegistry
{
	ILinkedMap<Object, IConnectionHolder> getPersistenceUnitToConnectionHolderMap();
}