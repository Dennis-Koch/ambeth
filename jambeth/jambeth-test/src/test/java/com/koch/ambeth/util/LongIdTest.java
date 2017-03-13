package com.koch.ambeth.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;

@SQLStructure("LongId_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/util/long-orm.xml")
public class LongIdTest extends AbstractInformationBusWithPersistenceTest
{
	// This test already succeeds if it reaches this point. We test a framework setup problem with Objects as member types.
	@Test
	public void test()
	{
		assertTrue(true); // Testing...testing...
	}
}
