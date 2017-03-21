package com.koch.ambeth.cache.server;

/*-
 * #%L
 * jambeth-cache-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
