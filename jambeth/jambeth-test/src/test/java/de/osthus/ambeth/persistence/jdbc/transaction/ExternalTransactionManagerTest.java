package de.osthus.ambeth.persistence.jdbc.transaction;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.transaction.ExternalTransactionManagerTest.ExternalTransactionManagerTestModule;
import de.osthus.ambeth.service.IMaterialService;
import de.osthus.ambeth.service.TestServicesModule;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@TestModule({ TestServicesModule.class })
@TestFrameworkModule(ExternalTransactionManagerTestModule.class)
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile, value = "de/osthus/ambeth/persistence/jdbc/mapping/value-object.xml"),
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true"),
		@TestProperties(name = PersistenceConfigurationConstants.ExternalTransactionManager, value = "true") })
@SQLStructure("../JDBCDatabase_structure.sql")
@SQLData("../Example_data.sql")
public class ExternalTransactionManagerTest extends AbstractInformationBusWithPersistenceTest
{
	@FrameworkModule
	public static class ExternalTransactionManagerTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAnonymousBean(TransactionManagerFake.class).autowireable(TransactionManager.class);
		}
	}

	@Autowired
	protected TransactionManager transactionManager;

	/**
	 * Test that external transaction is set to rollbackOnly when an exception occurs
	 * 
	 * @throws SystemException
	 *             should not happen, as transactionManager is a fake implementation
	 */
	@Test
	public void testRollback() throws SystemException
	{
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		Material material = materialService.getMaterialByName("test 1");
		material.setName("Updated");
		material.setVersion((short) (material.getVersion() - 1)); // Force OptLock exception

		try
		{
			materialService.updateMaterial(material);
			Assert.fail("Update should not have worked!");
		}
		catch (RuntimeException e)
		{
			// we want this exception for the test
		}

		TransactionFake transaction = (TransactionFake) transactionManager.getTransaction();
		Assert.assertTrue("Set rollbackOnly should be set to true", transaction.getRollbackOnly());
	}
}