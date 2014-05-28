package de.osthus.ambeth.persistence.event;

import java.util.List;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

@Service(IMultiEventService.class)
@PersistenceContext
public class MultiEventService implements IMultiEventService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private ICache cache;

	protected IEntityFactory entityFactory;

	/**
	 * This service, but wrapped in proxies
	 */
	private IMultiEventService multiEventService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(entityFactory, "entityFactory");
		ParamChecker.assertNotNull(multiEventService, "multiEventService");
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setEntityFactory(IEntityFactory entityFactory)
	{
		this.entityFactory = entityFactory;
	}

	public void setMultiEventService(IMultiEventService multiEventService)
	{
		this.multiEventService = multiEventService;
	}

	@Override
	public void save(List<MultiEventEntity> multiEventEntities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void doMultipleThings(List<MultiEventEntity> multiEventEntities)
	{
		multiEventService.save(multiEventEntities);

		MultiEventEntity2 multiEventEntity2 = cache.getObject(MultiEventEntity2.class, 1);
		String name = multiEventEntity2.getName();
		name = name.replace(".1", ".2");
		multiEventEntity2.setName(name);
		multiEventService.save(multiEventEntity2);
	}

	@Override
	public void doMultipleThings2(List<MultiEventEntity> multiEventEntities)
	{
		MultiEventEntity newEntity = entityFactory.createEntity(MultiEventEntity.class);
		newEntity.setName("Name 4.2");
		multiEventEntities.add(newEntity);

		multiEventService.save(multiEventEntities);

		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			name = name.replace(".2", ".3");
			entity.setName(name);
		}

		multiEventService.save(multiEventEntities);
	}

	@Override
	public void save(MultiEventEntity2 multiEventEntity2)
	{
		throw new UnsupportedOperationException();
	}
}
