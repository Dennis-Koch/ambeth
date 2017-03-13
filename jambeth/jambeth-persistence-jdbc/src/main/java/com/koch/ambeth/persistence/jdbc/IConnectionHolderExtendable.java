package com.koch.ambeth.persistence.jdbc;

import com.koch.ambeth.persistence.jdbc.IConnectionHolder;

public interface IConnectionHolderExtendable
{
	void registerConnectionHolder(IConnectionHolder connectionHolder, Object persistenceUnitId);

	void unregisterConnectionHolder(IConnectionHolder connectionHolder, Object persistenceUnitId);
}
