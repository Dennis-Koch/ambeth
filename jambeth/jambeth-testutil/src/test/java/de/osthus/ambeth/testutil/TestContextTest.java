package de.osthus.ambeth.testutil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class TestContextTest extends AbstractIocTest
{
	@Test
	public void testGetContextFileString()
	{
		String fileName = "TestContextTest.properties";
		File contextFile = testContext.getContextFile(fileName);
		assertNotNull(contextFile);
		assertTrue(contextFile.canRead());
	}

	@Test
	public void testGetContextFileStringClass()
	{
		String fileName = "TestContextTest.properties";
		File contextFile = testContext.getContextFile(fileName, TestContextTest.class);
		assertNotNull(contextFile);
		assertTrue(contextFile.canRead());
	}
}
