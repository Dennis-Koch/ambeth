package com.koch.ambeth.persistence.jdbc.synonym;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.SQLTableSynonyms;
import com.koch.ambeth.testutil.category.ReminderTests;

@Category(ReminderTests.class)
@SQLStructure("Synonym_structure.sql")
@SQLData("Synonym_data.sql")
@SQLTableSynonyms("S_CHILD")
public class SynonymTest extends AbstractInformationBusWithPersistenceTest {
	@LogInstance
	private ILogger log;

	@Test
	public void test1() {
		log.info("test1");
	}

	@Test
	public void test2() {
		log.info("test2");
	}
}
