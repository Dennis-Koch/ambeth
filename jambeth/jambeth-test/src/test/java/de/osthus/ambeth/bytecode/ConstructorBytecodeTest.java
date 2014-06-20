package de.osthus.ambeth.bytecode;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestRebuildContext;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/bytecode/ConstructorBytecodeTest-orm.xml")
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
