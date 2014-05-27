package de.osthus.ambeth.query.behavior;

import org.junit.Assert;

import de.osthus.ambeth.annotation.QueryBehavior;
import de.osthus.ambeth.annotation.QueryBehaviorType;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.util.ParamChecker;

@Service(IQueryBehaviorService.class)
public class QueryBehaviorService implements IInitializingBean, IStartingBean, IQueryBehaviorService
{
	private static final String QueryParamKey = "Key";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<Material> getMaterialByIdQuery;

	protected IQuery<Material> getAllMaterialsQuery;

	protected IQuery<Material> getMaterialByNameQuery;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(queryBuilderFactory, "QueryBuilderFactory");
	}

	@Override
	public void afterStarted() throws Throwable
	{
		IQueryBuilder<Material> getMaterialQB = queryBuilderFactory.create(Material.class);
		getMaterialByIdQuery = getMaterialQB.build(getMaterialQB.isEqualTo(getMaterialQB.property("Id"), getMaterialQB.valueName(QueryParamKey)));

		IQueryBuilder<Material> getAllMaterialsQB = queryBuilderFactory.create(Material.class);
		getAllMaterialsQuery = getAllMaterialsQB.build();

		IQueryBuilder<Material> getMaterialByNameQB = queryBuilderFactory.create(Material.class);
		getMaterialByNameQuery = getMaterialByNameQB.build(getMaterialByNameQB.isEqualTo(getMaterialByNameQB.property("Name"),
				getMaterialByNameQB.valueName(QueryParamKey)));
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory queryBuilderFactory)
	{
		this.queryBuilderFactory = queryBuilderFactory;
	}

	@Override
	public Material getMaterialByName(String name)
	{
		return getMaterialByNameQuery.param(QueryParamKey, name).retrieveSingle();
	}

	@Override
	@QueryBehavior(QueryBehaviorType.OBJREF_ONLY)
	public Material getMaterialByNameObjRefMode(String name)
	{
		Material material = getMaterialByName(name);
		Assert.assertNull(material);
		return material;
	}

	@Override
	@QueryBehavior(QueryBehaviorType.DEFAULT)
	public Material getMaterialByNameDefaultMode(String name)
	{
		Material material = getMaterialByName(name);
		Assert.assertNotNull(material);
		return material;
	}
}
