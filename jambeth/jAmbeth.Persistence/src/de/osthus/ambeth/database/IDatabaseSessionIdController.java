package de.osthus.ambeth.database;

public interface IDatabaseSessionIdController
{
	long acquireSessionId();

	void releaseSessionId(long sessionId);
}