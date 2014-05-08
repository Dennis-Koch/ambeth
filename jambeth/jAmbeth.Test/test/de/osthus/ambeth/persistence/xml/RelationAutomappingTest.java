package de.osthus.ambeth.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.persistence.xml.model.IBusinessService;
import de.osthus.ambeth.persistence.xml.model.IEmployeeService;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("/de/osthus/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/de/osthus/ambeth/persistence/xml/Relations_structure.sql")
@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/RelationAutomapping_orm.xml") })
@TestModule(TestServicesModule.class)
public class RelationAutomappingTest extends AbstractPersistenceTest
{
	protected IBusinessService businessService;

	protected ICache cache;

	protected IEmployeeService employeeService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(businessService, "BusinessService");
		ParamChecker.assertNotNull(cache, "Cache");
		ParamChecker.assertNotNull(employeeService, "EmployeeService");
	}

	public void setBusinessService(IBusinessService businessService)
	{
		this.businessService = businessService;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setEmployeeService(IEmployeeService employeeService)
	{
		this.employeeService = employeeService;
	}

	@Test
	public void testNullableToOne() throws Throwable
	{
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
