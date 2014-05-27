package de.osthus.ambeth.event;

public class DatabaseFailEvent
{

	protected final long sessionId;

	public long getSessionId()
	{
		return this.sessionId;
	}

	public DatabaseFailEvent(long sessionId)
	{
		this.sessionId = sessionId;
	}

}
