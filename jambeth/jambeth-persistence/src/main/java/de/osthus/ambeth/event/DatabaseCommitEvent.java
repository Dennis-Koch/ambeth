package de.osthus.ambeth.event;

public class DatabaseCommitEvent
{

	protected final long sessionId;

	public long getSessionId()
	{
		return this.sessionId;
	}

	public DatabaseCommitEvent(long sessionId)
	{
		this.sessionId = sessionId;
	}

}
