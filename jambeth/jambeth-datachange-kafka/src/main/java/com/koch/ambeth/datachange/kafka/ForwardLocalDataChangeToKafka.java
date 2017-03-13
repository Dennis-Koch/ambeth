package com.koch.ambeth.datachange.kafka;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class ForwardLocalDataChangeToKafka implements IEventListener
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEventListener eventListener;

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
	{
		if (!(eventObject instanceof IDataChange))
		{
			return;
		}
		IDataChange dataChange = (IDataChange) eventObject;
		if (dataChange.isEmpty() || !dataChange.isLocalSource())
		{
			return;
		}
		// ONLY forward events where localSource=true so the condition above is important
		// otherwise we would "bounce" a foreign event back to kafka in an endless loop
		eventListener.handleEvent(eventObject, dispatchTime, sequenceId);
	}
}
