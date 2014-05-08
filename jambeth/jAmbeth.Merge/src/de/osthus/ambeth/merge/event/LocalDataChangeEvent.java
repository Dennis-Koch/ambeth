package de.osthus.ambeth.merge.event;

import de.osthus.ambeth.datachange.model.IDataChange;

public class LocalDataChangeEvent
{
	private long sessionID;

	private IDataChange dataChange;

	public LocalDataChangeEvent(long sessionID, IDataChange dataChange)
	{
		setSessionID(sessionID);
		setDataChange(dataChange);
	}

	public long getSessionID()
	{
		return sessionID;
	}

	private void setSessionID(long sessionID)
	{
		this.sessionID = sessionID;
	}

	public IDataChange getDataChange()
	{
		return dataChange;
	}

	private void setDataChange(IDataChange dataChange)
	{
		this.dataChange = dataChange;
	}
}
