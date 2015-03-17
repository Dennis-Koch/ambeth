package de.osthus.ambeth.testutil;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.util.setup.IDataSetup;

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
