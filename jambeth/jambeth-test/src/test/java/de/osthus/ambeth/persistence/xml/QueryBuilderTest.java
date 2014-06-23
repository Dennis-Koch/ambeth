package de.osthus.ambeth.persistence.xml;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("/de/osthus/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/de/osthus/ambeth/persistence/xml/Relations_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm.xml")
@TestModule(TestServicesModule.class)
public class QueryBuilderTest extends AbstractPersistenceTest
{
	@Test
	public void testComplexQuery()
	{
		IQueryBuilder<Employee> queryBuilder = queryBuilderFactory.create(Employee.class);
		String paramName = "myParam";

		IQuery<Employee> query = queryBuilder.build(queryBuilder.isEqualTo(queryBuilder.property("PrimaryProject.Employees.OtherAddresses.Street"),
				queryBuilder.valueName(paramName), Boolean.FALSE));
		IList<Employee> employees = query.param(paramName, "TestStreet").retrieve();
		Assert.assertNotNull(employees);
	}
}
