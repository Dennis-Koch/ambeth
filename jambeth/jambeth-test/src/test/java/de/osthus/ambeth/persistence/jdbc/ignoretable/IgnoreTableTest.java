package de.osthus.ambeth.persistence.jdbc.ignoretable;

import org.junit.Test;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;

@SQLStructure("IgnoreTable_structure.sql")
public class IgnoreTableTest extends AbstractInformationBusWithPersistenceTest
{
	@Test
	public void testAutoIndexFalse()
	{
		transaction.processAndCommit(new DatabaseCallback()
		{

			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{

			}
		});
	}
}
