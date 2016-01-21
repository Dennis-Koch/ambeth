package de.osthus.ambeth.persistence.blueprint;

import java.util.List;

import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;
import de.osthus.ambeth.proxy.MergeContext;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;

@MergeContext
public class EntityTypeBluePrintService implements IInitializingBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IQueryBuilderFactory qbf;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	protected IQuery<EntityTypeBlueprint> qAll;

	protected IQuery<EntityTypeBlueprint> qByName;

	protected IPrefetchHandle typeToAllPrefetchHandle;

	@Find
	public List<EntityTypeBlueprint> getAll()
	{
		IList<EntityTypeBlueprint> list = qAll.retrieve();
		typeToAllPrefetchHandle.prefetch(list);
		return list;
	}

	@Find
	public EntityTypeBlueprint findByName(String name)
	{
		EntityTypeBlueprint entityTypeBlueprint = qByName.param(IEntityTypeBlueprint.NAME, name).retrieveSingle();
		typeToAllPrefetchHandle.prefetch(entityTypeBlueprint);
		return entityTypeBlueprint;
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{

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
}
