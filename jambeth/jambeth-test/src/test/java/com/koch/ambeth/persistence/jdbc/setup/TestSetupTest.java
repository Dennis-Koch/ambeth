package com.koch.ambeth.persistence.jdbc.setup;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;

/**
 * Empty test to quickly test structure and data files etc.
 */
@SQLData("TestSetup_data.sql")
@SQLStructure("TestSetup_structure.sql")
public class TestSetupTest extends AbstractInformationBusWithPersistenceTest
{
	@Test
	public void testDataSetup()
	{
		assertTrue(true);
	}
}
