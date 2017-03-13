package com.koch.ambeth.persistence.api;

public interface IDatabaseDisposeHook
{
	void databaseDisposed(IDatabase disposedDatabase);
}
