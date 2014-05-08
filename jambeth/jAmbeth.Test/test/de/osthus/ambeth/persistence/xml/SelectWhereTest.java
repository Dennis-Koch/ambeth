package de.osthus.ambeth.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.persistence.xml.model.IEmployeeService;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm.xml")
@TestModule(TestServicesModule.class)
public class SelectWhereTest extends AbstractPersistenceTest
{
	protected IEmployeeService employeeService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(employeeService, "employeeService");
	}

	public void setEmployeeService(IEmployeeService employeeService)
	{
		this.employeeService = employeeService;
	}

	@Test
	public void testSelectWhere() throws Throwable
	{
		List<Employee> sList = employeeService.retrieveOrderedByName(false);
		List<Employee> rList = employeeService.retrieveOrderedByName(true);

		assertFalse(sList.isEmpty());

		for (int i = sList.size(); i-- > 0;)
		{
			assertEquals(sList.get(i), rList.get(rList.size() - 1 - i));
		}
	}
}