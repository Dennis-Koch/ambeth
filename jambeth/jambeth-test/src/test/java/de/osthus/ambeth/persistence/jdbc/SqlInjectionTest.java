package de.osthus.ambeth.persistence.jdbc;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.service.IMaterialService;
import de.osthus.ambeth.service.TestServicesModule;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("Example_data.sql")
@SQLStructure("JDBCDatabase_structure.sql")
@TestModule(TestServicesModule.class)
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = CacheConfigurationConstants.FirstLevelCacheType, value = "PROTOTYPE") })
public class SqlInjectionTest extends AbstractInformationBusWithPersistenceTest
{
	protected IMaterialService materialService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(materialService, "materialService");
	}

	public void setMaterialService(IMaterialService materialService)
	{
		this.materialService = materialService;
	}

	@Test
	public void testInjectByMerge()
	{
		Material material = entityFactory.createEntity(Material.class);
		material.setName("Inject'; DROP TABLE MATERIAL;");
		materialService.updateMaterial(material);
		List<Material> allMaterials = materialService.getAllMaterials();
		Assert.assertTrue(material.getId() != 0);
		Assert.assertTrue(allMaterials.size() >= 1);
	}

	@Test
	public void testInjectByQuery2()
	{
		String name = "hallo";
		Material material = entityFactory.createEntity(Material.class);
		material.setName(name);
		materialService.updateMaterial(material);
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.isEqualTo(qb.property("Name"), qb.value(name)));
		IList<Material> materials = query.retrieve();
		Assert.assertTrue(materials.size() >= 1);
	}

	@Test
	public void testInjectByQuery()
	{
		String name = "Inject'; DROP TABLE MATERIAL;";
		Material material = entityFactory.createEntity(Material.class);
		material.setName(name);
		materialService.updateMaterial(material);
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.isEqualTo(qb.property("Name"), qb.value(name)));
		IList<Material> materials = query.retrieve();
		Assert.assertTrue(materials.size() >= 1);
	}
}
