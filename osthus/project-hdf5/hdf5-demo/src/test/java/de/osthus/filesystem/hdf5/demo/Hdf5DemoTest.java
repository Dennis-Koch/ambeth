package de.osthus.filesystem.hdf5.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-22
 */
public class Hdf5DemoTest
{
	private static final HashSet<String> EXPECTED_PROVIDERS = new HashSet<>(Arrays.asList("file", "jar", "hdf5"));

	@Test
	public void test()
	{
		List<FileSystemProvider> installedProviders = FileSystemProvider.installedProviders();
		assertEquals(3, installedProviders.size());
		for (FileSystemProvider provider : installedProviders)
		{
			String scheme = provider.getScheme();
			assertTrue("Unexpected provider for '" + scheme + "'", EXPECTED_PROVIDERS.contains(scheme));
		}
	}
}
