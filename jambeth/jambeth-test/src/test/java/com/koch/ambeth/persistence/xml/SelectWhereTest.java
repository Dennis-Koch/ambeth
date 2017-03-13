package com.koch.ambeth.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.persistence.xml.model.IEmployeeService;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/xml/orm.xml")
@TestModule(TestServicesModule.class)
public class SelectWhereTest extends AbstractInformationBusWithPersistenceTest
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