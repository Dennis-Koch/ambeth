package de.osthus.ambeth.mapping;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class MapperServiceFactory implements IMapperServiceFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICacheProvider cacheProvider;

	@Override
	public IMapperService create()
	{
		ICache cache = cacheProvider.getCurrentCache();
		IMapperService mapperService = beanContext.registerAnonymousBean(ModelTransferMapper.class).propertyValue("ChildCache", cache)
				.propertyValue("WritableCache", cache).finish();
		IMapperService mapperServiceReference = new MapperServiceWeakReference(mapperService);
		return mapperServiceReference;
	}
}
