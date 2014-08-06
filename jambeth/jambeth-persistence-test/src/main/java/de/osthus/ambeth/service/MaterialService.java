package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.util.ParamChecker;

@Service(IMaterialService.class)
@PersistenceContext
public class MaterialService implements IInitializingBean, IStartingBean, IMaterialService
{
	private static final String QueryParamKey = "Key";

	@SuppressWarnings("unused")
	@LogInstance(MaterialService.class)
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
	public Material getMaterial(int id)
	{
		return getMaterialByIdQuery.param(QueryParamKey, id).retrieveSingle();
	}

	@Override
	public List<Material> getAllMaterials()
	{
		return getAllMaterialsQuery.retrieve();
	}

	@Override
	public Material getMaterialByName(String name)
	{
		return getMaterialByNameQuery.param(QueryParamKey, name).retrieveSingle();
	}

	@Override
	public void updateMaterial(Material material)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateMaterials(Material[] materials)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteMaterial(Material material)
	{
		throw new UnsupportedOperationException();
	}
}
