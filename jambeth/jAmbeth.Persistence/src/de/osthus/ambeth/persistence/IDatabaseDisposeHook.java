package de.osthus.ambeth.persistence;

public interface IDatabaseDisposeHook
{
	void databaseDisposed(IDatabase disposedDatabase);
}
