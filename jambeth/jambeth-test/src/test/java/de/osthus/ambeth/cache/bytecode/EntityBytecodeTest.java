package de.osthus.ambeth.cache.bytecode;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.TestRebuildContext;

@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/cache/bytecode/EntityBytecodeTest-orm.xml") })
@TestRebuildContext
public class EntityBytecodeTest extends AbstractInformationBusTest
{
	@LogInstance
	protected ILogger log;

	@Autowired
	protected IRootCache rootCache;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Test
	public void testValueHolderWithoutField() throws Exception
	{
		TestEntity testEntity = entityFactory.createEntity(TestEntity.class);

		Assert.assertFalse(proxyHelper.isInitialized(testEntity, "ChildrenNoField"));
	}

	@Test
	public void testValueHolderWithProtectedField() throws Exception
	{
		TestEntity testEntity = entityFactory.createEntity(TestEntity.class);

		Assert.assertFalse(proxyHelper.isInitialized(testEntity, "ChildrenWithProtectedField"));
	}

	@Test
	public void testValueHolderWithPrivateField() throws Exception
	{
		TestEntity testEntity = entityFactory.createEntity(TestEntity.class);

		Assert.assertFalse(proxyHelper.isInitialized(testEntity, "ChildrenWithPrivateField"));
	}

	@Test
	public void testInterfaceEntity() throws Exception
	{
		ITestEntity2 testEntity = entityFactory.createEntity(ITestEntity2.class);

		Assert.assertFalse(proxyHelper.isInitialized(testEntity, "ChildrenWithProtectedField"));
	}

	@Test
	public void testInterfaceEntityReadDate() throws Exception
	{
		ITestEntity2 testEntity = entityFactory.createEntity(ITestEntity2.class);

		testEntity.setMyDate(new Date(System.currentTimeMillis()));

		testEntity.setId(1);
		testEntity.setVersion(1);

		rootCache.put(testEntity);

		Assert.assertFalse(proxyHelper.isInitialized(testEntity, "ChildrenWithProtectedField"));
	}
}
