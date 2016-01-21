package de.osthus.ambeth.persistence.blueprint;

import java.util.List;

import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.Merge;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ReadWriteLock;

@SecurityContext(SecurityContextType.NOT_REQUIRED)
public class EntityTypeBluePrintService implements IStartingBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IQueryBuilderFactory qbf;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected ICacheIntern cache;

	protected ReadWriteLock lock = new ReadWriteLock();

	protected IQuery<EntityTypeBlueprint> qAll;

	protected IQuery<EntityTypeBlueprint> qByName;

	protected IPrefetchHandle typeToAllPrefetchHandle;

	@Merge
	public void saveType(EntityTypeBlueprint entityTypeBlueprint)
	{
		throw new UnsupportedOperationException("Must never be called");
	}

	@Find
	public List<EntityTypeBlueprint> getAll()
	{
		if (qAll == null)
		{
			initializeQueries();
		}
		IList<EntityTypeBlueprint> list = qAll.retrieve();
		typeToAllPrefetchHandle.prefetch(list);
		return list;
	}

	@Find
	public EntityTypeBlueprint findByName(String name)
	{
		if (qByName != null)
		{
			initializeQueries();
		}
		EntityTypeBlueprint entityTypeBlueprint = qByName.param(IEntityTypeBlueprint.NAME, name).retrieveSingle();
		typeToAllPrefetchHandle.prefetch(entityTypeBlueprint);
		return entityTypeBlueprint;
	}

	@Override
	public void afterStarted() throws Throwable
	{
		initializeQueries();
	}

	protected void initializeQueries()
	{
		if (qAll != null)
		{
			return;
		}
		lock.getWriteLock().lock();
		try
		{
			if (qAll != null)
			{
				return;
			}
			IPrefetchConfig prefetchConfig = prefetchHelper.createPrefetch();
			EntityTypeBlueprint plan = prefetchConfig.plan(EntityTypeBlueprint.class);
			plan.getProperties().iterator().next().getAnnotations().iterator().next().getProperties().iterator().next();
			plan.getAnnotations().iterator().next().getProperties().iterator().next();
			typeToAllPrefetchHandle = prefetchConfig.build();

			IQueryBuilder<EntityTypeBlueprint> qb = qbf.create(EntityTypeBlueprint.class);
			qAll = qb.build();

			qb = qbf.create(EntityTypeBlueprint.class);
			qByName = qb.build(qb.isEqualTo(qb.property(IEntityTypeBlueprint.NAME), qb.valueName(IEntityTypeBlueprint.NAME)));
		}
		finally
		{
			lock.releaseAllLocks();
		}
	}
}
