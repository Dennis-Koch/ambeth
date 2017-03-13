package com.koch.ambeth.persistence.event;

public class DatabaseCommitEvent implements IDatabaseReleaseEvent
{
	protected final long sessionId;

	@Override
	public long getSessionId()
	{
		return this.sessionId;
	}

	public DatabaseCommitEvent(long sessionId)
	{
		this.sessionId = sessionId;
	}
}
