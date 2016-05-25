package de.osthus.ambeth.datachange.kafka;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class DataChangeKafkaBridge implements IEventListener
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
		eventListener.handleEvent(eventObject, dispatchTime, sequenceId);
	}
}
