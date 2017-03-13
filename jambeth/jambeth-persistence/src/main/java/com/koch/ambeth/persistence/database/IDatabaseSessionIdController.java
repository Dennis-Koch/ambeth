package com.koch.ambeth.persistence.database;

public interface IDatabaseSessionIdController
{
	long acquireSessionId();

	void releaseSessionId(long sessionId);
}