package de.osthus.ambeth.cache;

import java.util.Arrays;
import java.util.List;

import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.event.IEventTargetEventListener;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.merge.event.LocalDataChangeEvent;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

public class CacheLocalDataChangeListener implements IEventListener, IEventTargetEventListener, IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected IEventListener cacheDataChangeListener;

	protected IEventTargetEventListener etCacheDataChangeListener;

	protected IThreadLocalObjectCollector objectCollector;

	protected ITransactionState transactionState;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(cacheDataChangeListener, "CacheDataChangeListener");
		ParamChecker.assertNotNull(etCacheDataChangeListener, "EtCacheDataChangeListener");
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
		ParamChecker.assertNotNull(transactionState, "TransactionState");
	}

	public void setCacheDataChangeListener(IEventListener cacheDataChangeListener)
	{
		this.cacheDataChangeListener = cacheDataChangeListener;
	}

	public void setEtCacheDataChangeListener(IEventTargetEventListener etCacheDataChangeListener)
	{
		this.etCacheDataChangeListener = etCacheDataChangeListener;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setTransactionState(ITransactionState transactionState)
	{
		this.transactionState = transactionState;
	}

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
		if (!(eventObject instanceof LocalDataChangeEvent))
		{
			return;
		}
		if (!isInTransaction())
		{
			return;
		}
		LocalDataChangeEvent localDCE = (LocalDataChangeEvent) eventObject;
		if (log.isDebugEnabled())
		{
			log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "handleEvent() Transactional DCE: ", localDCE.getDataChange()));
		}
		cacheDataChangeListener.handleEvent(localDCE.getDataChange(), dispatchTime, sequenceId);
	}

	@Override
	public void handleEvent(Object eventObject, Object eventTarget, List<Object> pausedEventTargets, long dispatchTime, long sequenceId) throws Exception
	{
		if (!(eventObject instanceof LocalDataChangeEvent))
		{
			return;
		}
		if (!isInTransaction())
		{
			return;
		}
		LocalDataChangeEvent localDCE = (LocalDataChangeEvent) eventObject;
		if (log.isDebugEnabled())
		{
			log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "handleEvent() Transactional DCE: ", localDCE.getDataChange(), ", ET:",
					eventTarget, ", Paused ETs:", Arrays.toString(pausedEventTargets.toArray())));
		}
		etCacheDataChangeListener.handleEvent(localDCE.getDataChange(), eventTarget, pausedEventTargets, dispatchTime, sequenceId);
	}
}
