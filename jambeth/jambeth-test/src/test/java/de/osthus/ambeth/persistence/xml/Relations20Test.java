package de.osthus.ambeth.persistence.xml;

import org.junit.Test;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm20.xml")
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
