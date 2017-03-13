package com.koch.ambeth.persistence.jdbc.ignoretable;

import org.junit.Test;

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.util.collections.ILinkedMap;

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
