package com.koch.ambeth.persistence.update;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.service.proxy.Service;

@Service(EntityCService.class)
@PersistenceContext
public class EntityCService implements IEntityCService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(EntityCService.class)
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public void save(EntityC entity)
	{
		throw new UnsupportedOperationException();
	}
}
