package de.osthus.ambeth.persistence.update;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

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
