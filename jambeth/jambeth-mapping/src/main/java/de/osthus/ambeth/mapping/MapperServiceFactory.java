package de.osthus.ambeth.mapping;

import de.osthus.ambeth.garbageproxy.IGarbageProxyConstructor;
import de.osthus.ambeth.garbageproxy.IGarbageProxyFactory;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class MapperServiceFactory implements IMapperServiceFactory, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IGarbageProxyFactory garbageProxyFactory;

	protected IGarbageProxyConstructor<IMapperService> mapperServiceGPC;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		mapperServiceGPC = garbageProxyFactory.createGarbageProxyConstructor(IMapperService.class);
	}

	@Override
	public IMapperService create()
	{
		IMapperService mapperService = beanContext.registerBean(ModelTransferMapper.class).finish();
		return mapperServiceGPC.createInstance(mapperService);
	}
}
