package com.koch.ambeth.persistence.api.database;

public interface ITransactionListener
{
	void handlePostBegin(long sessionId) throws Throwable;

	void handlePreCommit(long sessionId) throws Throwable;

	void handlePostRollback(long sessionId) throws Throwable;
}
