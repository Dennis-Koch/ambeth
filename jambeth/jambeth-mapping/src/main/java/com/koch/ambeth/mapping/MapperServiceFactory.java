package com.koch.ambeth.mapping;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyConstructor;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyFactory;
import com.koch.ambeth.util.TypeUtil;

public class MapperServiceFactory implements IMapperServiceFactory {
	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IGarbageProxyFactory garbageProxyFactory;

	protected IGarbageProxyConstructor<IMapperService> mapperServiceGPC;

	@Override
	public IMapperService create() {
		if (mapperServiceGPC == null) {
			mapperServiceGPC = garbageProxyFactory.createGarbageProxyConstructor(IMapperService.class,
					TypeUtil.EMPTY_TYPES);
		}
		IMapperService mapperService = beanContext.registerBean(ModelTransferMapper.class).finish();
		return mapperServiceGPC.createInstance(mapperService);
	}
}
