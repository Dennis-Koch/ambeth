package de.osthus.ambeth.event;

public class DatabaseAcquireEvent implements IDatabaseSessionAwareEvent
{
	protected final long sessionId;

	@Override
	public long getSessionId()
	{
		return this.sessionId;
	}

	public DatabaseAcquireEvent(long sessionId)
	{
		this.sessionId = sessionId;
	}
}
