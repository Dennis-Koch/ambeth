package de.osthus.ambeth.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@SQLStructure("LongId_structure.sql")
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/util/long-orm.xml")
public class LongIdTest extends AbstractPersistenceTest
{
	// This test already succeeds if it reaches this point. We test a framework setup problem with Objects as member types.
	@Test
	public void test()
	{
		assertTrue(true); // Testing...testing...
	}
}
