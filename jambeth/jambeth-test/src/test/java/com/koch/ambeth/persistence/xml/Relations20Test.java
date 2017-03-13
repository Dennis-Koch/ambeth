package com.koch.ambeth.persistence.xml;

import org.junit.Test;

import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.collections.HashMap;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/xml/orm20.xml")
public class Relations20Test extends RelationsTest
{
	@Test
	public void testHashMapProperty()
	{
		Employee employee = cache.getObject(Employee.class, 1);
		employee.setAttributes(new HashMap<Object, Object>());
		employee.getAttributes().put("hallo", "welt");
		employee.setName("name");
		employeeService.save(employee);
	}
}
