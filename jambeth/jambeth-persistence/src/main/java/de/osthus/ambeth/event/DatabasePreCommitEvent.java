package de.osthus.ambeth.event;

public class DatabasePreCommitEvent implements IDatabaseSessionAwareEvent
{
	protected final long sessionId;

	@Override
	public long getSessionId()
	{
		return this.sessionId;
	}

	public DatabasePreCommitEvent(long sessionId)
	{
		this.sessionId = sessionId;
	}
}
