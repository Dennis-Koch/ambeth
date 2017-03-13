package com.koch.ambeth.persistence.update;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.ParamChecker;

@Service(IEntityAService.class)
@PersistenceContext
public class EntityAService implements IEntityAService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	// 'this' but with proxies
	private IEntityAService entityAService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(entityAService, "entityAService");
	}

	public void setEntityAService(IEntityAService entityAService)
	{
		this.entityAService = entityAService;
	}

	@Override
	public void save(EntityA entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAndReSetEntityD(EntityA entity)
	{
		EntityD entityD = entity.getEntityD();
		entityD.getId();
		entity.setEntityD(null);
		entityAService.save(entity);
		entity.setEntityD(entityD);
		entityAService.save(entity);
	}
}
