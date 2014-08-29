package de.osthus.ambeth.merge.event;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeOfSession;

public class DataChangeOfSession implements IDataChangeOfSession
{
	protected final long sessionId;

	protected final IDataChange dataChange;

	public DataChangeOfSession(long sessionId, IDataChange dataChange)
	{
		this.sessionId = sessionId;
		this.dataChange = dataChange;
	}

	@Override
	public long getSessionId()
	{
		return sessionId;
	}

	@Override
	public IDataChange getDataChange()
	{
		return dataChange;
	}
}
