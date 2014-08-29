package de.osthus.ambeth.cache;

import java.util.Arrays;
import java.util.List;

import de.osthus.ambeth.datachange.model.IDataChangeOfSession;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.event.IEventTargetEventListener;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.merge.event.DataChangeOfSession;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringBuilderUtil;

public class CacheLocalDataChangeListener implements IEventListener, IEventTargetEventListener
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEventListener cacheDataChangeListener;

	@Autowired
	protected IEventTargetEventListener etCacheDataChangeListener;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ITransactionState transactionState;

	protected boolean isInTransaction()
	{
		if (transactionState != null)
		{
			return transactionState.isTransactionActive();
		}
		return false;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
	{
		if (!(eventObject instanceof IDataChangeOfSession))
		{
			return;
		}
		if (!isInTransaction())
		{
			return;
		}
		IDataChangeOfSession localDCE = (IDataChangeOfSession) eventObject;
		if (log.isDebugEnabled())
		{
			log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "handleEvent() Transactional DCE: ", localDCE.getDataChange()));
		}
		cacheDataChangeListener.handleEvent(localDCE.getDataChange(), dispatchTime, sequenceId);
	}

	@Override
	public void handleEvent(Object eventObject, Object eventTarget, List<Object> pausedEventTargets, long dispatchTime, long sequenceId) throws Exception
	{
		if (!(eventObject instanceof IDataChangeOfSession))
		{
			return;
		}
		if (!isInTransaction())
		{
			return;
		}
		DataChangeOfSession localDCE = (DataChangeOfSession) eventObject;
		if (log.isDebugEnabled())
		{
			log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "handleEvent() Transactional DCE: ", localDCE.getDataChange(), ", ET:",
					eventTarget, ", Paused ETs:", Arrays.toString(pausedEventTargets.toArray())));
		}
		etCacheDataChangeListener.handleEvent(localDCE.getDataChange(), eventTarget, pausedEventTargets, dispatchTime, sequenceId);
	}
}
