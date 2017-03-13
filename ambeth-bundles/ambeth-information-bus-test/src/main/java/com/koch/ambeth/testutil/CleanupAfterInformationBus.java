package com.koch.ambeth.testutil;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.util.setup.IDataSetup;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.testutil.CleanupAfterIoc;

public class CleanupAfterInformationBus extends CleanupAfterIoc
{
	@Autowired(CacheModule.COMMITTED_ROOT_CACHE)
	protected IRootCache committedRootCache;

	@Autowired(optional = true)
	protected IDataSetup dataSetup;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Override
	public void cleanup()
	{
		if (dataSetup != null)
		{
			dataSetup.eraseEntityReferences();
		}
		committedRootCache.clear();
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		super.cleanup();
	}
}
