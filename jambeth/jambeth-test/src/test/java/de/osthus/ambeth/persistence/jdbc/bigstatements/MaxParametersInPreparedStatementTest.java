package de.osthus.ambeth.persistence.jdbc.bigstatements;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
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
public class MaxParametersInPreparedStatementTest extends AbstractInformationBusWithPersistenceTest
{

	@Autowired
	protected ICache cache;

	/**
	 * This test is necessary to test the "maximal parameter" limitation of postgresql (it should work in any other database back end as well)
	 * 
	 * @throws Exception
	 */
	@Test
	public void test40000ParemetersInPreparedStatementTest() throws Exception
	{
		String paramName = "paramName";
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.isIn(qb.property("Id"), qb.valueName(paramName)));

		ArrayList<Object> bigList = new ArrayList<Object>();
		for (int a = 40000; a-- > 0;)
		{
			bigList.add(Integer.valueOf(a + 1));
		}
		// this getObjects call leads Ambeth to create a query with 40000 parameters, this is more then the expected limit in postgres (32.000...) if this call
		// does not throw an exception, it works (no further assertions needed)
		cache.getObjects(Material.class, bigList);
	}
}
