package de.osthus.ambeth.persistence.jdbc.synonym;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.SQLTableSynonyms;
import de.osthus.ambeth.testutil.category.ReminderTests;

@Category(ReminderTests.class)
@SQLStructure("Synonym_structure.sql")
@SQLData("Synonym_data.sql")
@SQLTableSynonyms("S_CHILD")
public class SynonymTest extends AbstractPersistenceTest
{
	@Test
	public void test1()
	{
		System.out.println();
		System.out.println("test1");
	}

	@Test
	public void test2()
	{
		System.out.println();
		System.out.println("test2");
	}
}
