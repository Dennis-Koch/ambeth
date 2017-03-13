package com.koch.ambeth.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.persistence.xml.model.IBusinessService;
import com.koch.ambeth.persistence.xml.model.IEmployeeService;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.ReminderTests;

@Category(ReminderTests.class)
@SQLData("/com/koch/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/com/koch/ambeth/persistence/xml/Relations_structure.sql")
@TestPropertiesList({@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/xml/RelationAutomapping_orm.xml")})
@TestModule(TestServicesModule.class)
public class RelationAutomappingTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	protected IBusinessService businessService;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IEmployeeService employeeService;

	@Test
	public void testNullableToOne() throws Throwable {
		Employee employee1 = employeeService.getByName("Oscar Meyer");
		Employee employee2 = employeeService.getByName("Steve Smith");

		assertNull(employee1.getSupervisor());
		assertNotNull(employee2.getSupervisor());
		assertEquals(employee1.getId(), employee2.getSupervisor().getId());

		employee2.setSupervisor(null);
		employee1.setSupervisor(employee2);
		employeeService.save(employee1);
		employeeService.save(employee2);

		Employee loadedEmployee1 = employeeService.getByName("Oscar Meyer");
		Employee loadedEmployee2 = employeeService.getByName("Steve Smith");

		assertNull(loadedEmployee2.getSupervisor());
		assertNotNull(loadedEmployee1.getSupervisor());
		assertEquals(loadedEmployee2.getId(), loadedEmployee1.getSupervisor().getId());
	}
}
