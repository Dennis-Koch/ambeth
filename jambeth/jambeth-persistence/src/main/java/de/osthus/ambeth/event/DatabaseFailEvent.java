package de.osthus.ambeth.event;

public class DatabaseFailEvent implements IDatabaseReleaseEvent
{
	protected final long sessionId;

	@Override
	public long getSessionId()
	{
		return this.sessionId;
	}

	public DatabaseFailEvent(long sessionId)
	{
		this.sessionId = sessionId;
	}
}
