package com.koch.ambeth.persistence.jdbc.transaction;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.transaction.ExternalTransactionManagerTest.ExternalTransactionManagerTestModule;
import com.koch.ambeth.service.IMaterialService;
import com.koch.ambeth.service.TestServicesModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;

@TestModule({TestServicesModule.class})
@TestFrameworkModule(ExternalTransactionManagerTestModule.class)
@TestPropertiesList({
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile,
				value = "com/koch/ambeth/persistence/jdbc/mapping/value-object.xml"),
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true"),
		@TestProperties(name = PersistenceConfigurationConstants.ExternalTransactionManager,
				value = "true")})
@SQLStructure("../JDBCDatabase_structure.sql")
@SQLData("../Example_data.sql")
public class ExternalTransactionManagerTest extends AbstractInformationBusWithPersistenceTest {
	@FrameworkModule
	public static class ExternalTransactionManagerTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerAnonymousBean(TransactionManagerFake.class)
					.autowireable(TransactionManager.class);
		}
	}

	@Autowired
	protected TransactionManager transactionManager;

	/**
	 * Test that external transaction is set to rollbackOnly when an exception occurs
	 *
	 * @throws SystemException should not happen, as transactionManager is a fake implementation
	 */
	@Test
	public void testRollback() throws SystemException {
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		Material material = materialService.getMaterialByName("test 1");
		material.setName("Updated");
		material.setVersion((short) (material.getVersion() - 1)); // Force OptLock exception

		try {
			materialService.updateMaterial(material);
			Assert.fail("Update should not have worked!");
		}
		catch (RuntimeException e) {
			// we want this exception for the test
		}

		TransactionFake transaction = (TransactionFake) transactionManager.getTransaction();
		Assert.assertTrue("Set rollbackOnly should be set to true", transaction.getRollbackOnly());
	}
}
