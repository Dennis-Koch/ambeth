package de.osthus.ambeth.persistence.update;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;

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
