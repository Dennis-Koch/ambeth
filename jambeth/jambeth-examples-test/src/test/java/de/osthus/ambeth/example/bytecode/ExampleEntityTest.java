package de.osthus.ambeth.example.bytecode;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "ExampleEntityTest_orm.xml")
public class ExampleEntityTest extends AbstractInformationBusTest
{
	@Autowired
	protected IEntityFactory entityFactory;

	@Test
	public void myTest()
	{
		ExampleEntity exampleEntity = entityFactory.createEntity(ExampleEntity.class);
		System.out.println(exampleEntity);
	}
}
