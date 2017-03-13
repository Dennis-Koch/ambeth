package com.koch.ambeth.persistence.blueprint;

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.orm.blueprint.IEntityTypeBlueprint;
import com.koch.ambeth.merge.proxy.MergeContext;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.collections.IList;

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
