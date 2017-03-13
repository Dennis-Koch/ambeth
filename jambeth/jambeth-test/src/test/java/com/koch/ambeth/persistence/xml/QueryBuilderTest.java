package com.koch.ambeth.persistence.xml;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.collections.IList;

@SQLData("/com/koch/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/com/koch/ambeth/persistence/xml/Relations_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/xml/orm.xml")
@TestModule(TestServicesModule.class)
public class QueryBuilderTest extends AbstractInformationBusWithPersistenceTest
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
