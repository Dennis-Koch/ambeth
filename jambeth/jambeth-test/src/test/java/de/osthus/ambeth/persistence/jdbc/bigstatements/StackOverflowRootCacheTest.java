package de.osthus.ambeth.persistence.jdbc.bigstatements;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.model.Unit;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.category.SpecialTests;

@Category(SpecialTests.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseHost, value = "localhost"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseName, value = "jambeth"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseUser, value = "jambeth"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabasePass, value = "jambeth"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName, value = "jambeth"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol, value = "jdbc:postgresql"),
		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabasePort, value = "1531"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.JdbcTable", value = "DEBUG") })
@SQLStructure("BigStatement_structure.sql")
@SQLData("MaxParameters_data.sql")
@SQLDataRebuild(true)
public class StackOverflowRootCacheTest extends AbstractInformationBusWithPersistenceTest
{

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IMergeProcess mergeProcess;

	/**
	 * This test is necessary to test the "maximal parameter" limitation of postgresql (it should work in any other database back end as well)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStackOverflowRootCache() throws Exception
	{
		Material material;
		MaterialGroup materialGroup;
		Unit unit;
		ArrayList<Object> bigList = new ArrayList<Object>();
		for (int a = 400000; a-- > 0;)
		{
			material = entityFactory.createEntity(Material.class);
			material.setName("hugo");

			unit = entityFactory.createEntity(Unit.class);
			unit.setName("hugo2");

			materialGroup = entityFactory.createEntity(MaterialGroup.class);
			materialGroup.setName("asdf");

			material.setMaterialGroup(materialGroup);
			material.setUnit(unit);
			bigList.add(material);
		}
		mergeProcess.process(bigList, null, null, null, true);

	}
}
