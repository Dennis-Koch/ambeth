package de.osthus.ambeth.persistence.jdbc.setup;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;

/**
 * Empty test to quickly test structure and data files etc.
 */
@SQLData("TestSetup_data.sql")
@SQLStructure("TestSetup_structure.sql")
public class TestSetupTest extends AbstractPersistenceTest
{
	@Test
	public void testDataSetup()
	{
		assertTrue(true);
	}
}
