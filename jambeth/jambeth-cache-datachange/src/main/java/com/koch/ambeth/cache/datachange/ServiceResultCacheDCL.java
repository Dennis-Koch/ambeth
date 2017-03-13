package com.koch.ambeth.cache.datachange;

import com.koch.ambeth.cache.IServiceResultCache;
import com.koch.ambeth.datachange.IDataChangeListener;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class ServiceResultCacheDCL implements IDataChangeListener
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceResultCache serviceResultCache;

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId)
	{
		if (dataChange.isEmpty())
		{
			return;
		}
		serviceResultCache.invalidateAll();
	}
}
