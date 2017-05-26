package com.koch.ambeth.persistence.jdbc.auto;

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

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.collections.ILinkedMap;

@SQLData("autoindex_data.sql")
@SQLStructure("autoindex_structure.sql")
@TestPropertiesList({
		@TestProperties(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, value = "false"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "com/koch/ambeth/persistence/jdbc/auto/autoindex_orm.xml")})
public class AutoIndexFalseTest extends AbstractInformationBusWithPersistenceTest {
	@Test
	public void testAutoIndexFalse() {
		transaction.processAndCommit(new DatabaseCallback() {

			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
					throws Exception {
				int countOfUnindexedFKs = AutoIndexTrueTest.getCountOfUnindexedFKs(beanContext);
				Assert.assertEquals(1, countOfUnindexedFKs);
			}
		});
	}
}
