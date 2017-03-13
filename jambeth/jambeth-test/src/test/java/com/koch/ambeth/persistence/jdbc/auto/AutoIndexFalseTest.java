package com.koch.ambeth.persistence.jdbc.auto;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.collections.ILinkedMap;

@SQLData("autoindex_data.sql")
@SQLStructure("autoindex_structure.sql")
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, value = "false"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/jdbc/auto/autoindex_orm.xml") })
public class AutoIndexFalseTest extends AbstractInformationBusWithPersistenceTest
{
	@Test
	public void testAutoIndexFalse()
	{
		transaction.processAndCommit(new DatabaseCallback()
		{

			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{
				int countOfUnindexedFKs = AutoIndexTrueTest.getCountOfUnindexedFKs(beanContext);
				Assert.assertEquals(1, countOfUnindexedFKs);
			}
		});
	}
}
