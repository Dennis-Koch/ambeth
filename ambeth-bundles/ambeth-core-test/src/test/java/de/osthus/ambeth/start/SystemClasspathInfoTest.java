package de.osthus.ambeth.start;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.collections.IList;

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
		IList<URL> jarURLs = systemClasspathInfo.getJarURLs();
		Assert.assertNotNull(jarURLs);
		Assert.assertFalse(jarURLs.isEmpty());
	}

	@Test
	public void testOpenAsFile() throws Throwable
	{
		IList<URL> jarURLs = systemClasspathInfo.getJarURLs();
		URL url = jarURLs.get(jarURLs.size() - 1);
		Path file = systemClasspathInfo.openAsFile(url);
		Assert.assertNotNull(file);
		Assert.assertTrue(Files.isReadable(file));
	}

	@Test
	public void testOpenAsFile_pathWithSpace() throws Throwable
	{
		String filePath = "file:/home/user/name with space/lib";
		URL url = new URL(filePath);

		Path file = systemClasspathInfo.openAsFile(url);
		Assert.assertNotNull(file);
	}

	@Test
	public void testOpenAsFile_TestFirstPartNotStripped() throws Throwable
	{
		String expectedResult = "C:\\home\\jenkins\\data\\workspace\\jAmbeth-debug\\osthus-ambeth\\ambeth-bundles\\ambeth-core-test\\target\\test-classes\\pluginScanResource\\modules.jar";
		URL testUrl = new URL(
				"file:///home/jenkins/data/workspace/jAmbeth-debug/osthus-ambeth/ambeth-bundles/ambeth-core-test/target/test-classes/pluginScanResource/modules.jar");

		Path result = systemClasspathInfo.openAsFile(testUrl);

		Assert.assertEquals(expectedResult, result.toString());
	}
}
