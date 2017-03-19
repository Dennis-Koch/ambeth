package com.koch.ambeth.example.bytecode;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "ExampleEntityTest_orm.xml")
public class ExampleEntityTest extends AbstractInformationBusTest {
	@Autowired
	protected IEntityFactory entityFactory;

	@Test
	public void myTest() {
		ExampleEntity exampleEntity = entityFactory.createEntity(ExampleEntity.class);
		Assert.assertEquals(ExampleEntity.class.getName() + "-null", exampleEntity.toString());
	}
}
