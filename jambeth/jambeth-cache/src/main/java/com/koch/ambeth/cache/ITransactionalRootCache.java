package com.koch.ambeth.cache;

public interface ITransactionalRootCache
{
	void acquireTransactionalRootCache();

	void disposeTransactionalRootCache(boolean success);
}