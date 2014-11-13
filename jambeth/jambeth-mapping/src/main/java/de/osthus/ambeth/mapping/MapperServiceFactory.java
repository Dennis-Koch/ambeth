package de.osthus.ambeth.mapping;

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

	@Override
	public IMapperService create()
	{
		IMapperService mapperService = beanContext.registerBean(ModelTransferMapper.class).finish();
		IMapperService mapperServiceReference = new MapperServiceWeakReference(mapperService);
		return mapperServiceReference;
	}
}
