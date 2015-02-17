package de.osthus.ambeth.persistence.jdbc.synonym;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.SQLTableSynonyms;
import de.osthus.ambeth.testutil.category.ReminderTests;

@Category(ReminderTests.class)
@SQLStructure("Synonym_structure.sql")
@SQLData("Synonym_data.sql")
@SQLTableSynonyms("S_CHILD")
public class SynonymTest extends AbstractInformationBusWithPersistenceTest
{
	@LogInstance
	private ILogger log;

	@Test
	public void test1()
	{
		log.info("test1");
	}

	@Test
	public void test2()
	{
		log.info("test2");
	}
}
