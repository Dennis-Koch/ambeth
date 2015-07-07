package de.osthus.ambeth.start;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;

public class SystemClasspathInfoTest
{
	private SystemClasspathInfo systemClasspathInfo;

	@Before
	public void setUp() throws Exception
	{
		systemClasspathInfo = new SystemClasspathInfo();
	}

	@Test
	public void testGetJarURLs()
	{
		ArrayList<URL> jarURLs = systemClasspathInfo.getJarURLs();
		Assert.assertNotNull(jarURLs);
		Assert.assertFalse(jarURLs.isEmpty());
	}

	@Test
	public void testOpenAsFile() throws Throwable
	{
		ArrayList<URL> jarURLs = systemClasspathInfo.getJarURLs();
		URL url = jarURLs.get(0);

		File file = systemClasspathInfo.openAsFile(url);
		Assert.assertNotNull(file);
		Assert.assertTrue(file.canRead());
	}
}
