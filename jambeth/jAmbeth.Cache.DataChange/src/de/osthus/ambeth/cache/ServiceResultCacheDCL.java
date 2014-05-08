package de.osthus.ambeth.cache;

import de.osthus.ambeth.datachange.IDataChangeListener;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class ServiceResultCacheDCL implements IDataChangeListener, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(ServiceResultCacheDCL.class)
	private ILogger log;

	protected IServiceResultCache serviceResultCache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(serviceResultCache, "serviceResultCache");
	}

	public void setServiceResultCache(IServiceResultCache serviceResultCache)
	{
		this.serviceResultCache = serviceResultCache;
	}

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
