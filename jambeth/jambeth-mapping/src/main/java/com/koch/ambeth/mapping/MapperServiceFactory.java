package com.koch.ambeth.mapping;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyConstructor;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
