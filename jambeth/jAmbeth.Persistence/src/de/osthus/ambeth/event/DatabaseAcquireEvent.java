package de.osthus.ambeth.event;

public class DatabaseAcquireEvent
{

	protected final long sessionId;

	public long getSessionId()
	{
		return this.sessionId;
	}

	public DatabaseAcquireEvent(long sessionId)
	{
		this.sessionId = sessionId;
	}

}
