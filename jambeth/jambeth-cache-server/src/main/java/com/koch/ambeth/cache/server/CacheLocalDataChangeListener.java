package com.koch.ambeth.cache.server;

import java.util.Arrays;
import java.util.List;

import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventTargetEventListener;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.event.DataChangeOfSession;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

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
