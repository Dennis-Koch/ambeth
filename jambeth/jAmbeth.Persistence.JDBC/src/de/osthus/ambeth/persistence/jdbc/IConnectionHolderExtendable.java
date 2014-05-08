package de.osthus.ambeth.persistence.jdbc;

public interface IConnectionHolderExtendable
{
	void registerConnectionHolder(IConnectionHolder connectionHolder, Object persistenceUnitId);

	void unregisterConnectionHolder(IConnectionHolder connectionHolder, Object persistenceUnitId);
}
