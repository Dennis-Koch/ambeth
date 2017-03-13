package com.koch.ambeth.bytecode;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.model.IDataObject;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/bytecode/ConstructorBytecodeTest-orm.xml")
@TestRebuildContext
public class ConstructorBytecodeTest extends AbstractInformationBusTest
{
	@Autowired
	protected IEntityFactory entityFactory;

	@Test
	public void testWithNonDefaultConstructor() throws Exception
	{
		TestEntityWithNonDefaultConstructor testEntity = entityFactory.createEntity(TestEntityWithNonDefaultConstructor.class);

		assertNotNull(testEntity);
	}

	@Test
	public void testIsToBeCreated() throws Exception
	{
		TestEntityWithNonDefaultConstructor testEntity = entityFactory.createEntity(TestEntityWithNonDefaultConstructor.class);

		assertTrue(testEntity instanceof IDataObject);
		assertTrue(((IDataObject) testEntity).hasPendingChanges());
	}
}
