package de.osthus.ambeth.persistence.jdbc.auto;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@SQLData("autoindex_data.sql")
@SQLStructure("autoindex_structure.sql")
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, value = "false"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/jdbc/auto/autoindex_orm.xml") })
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
