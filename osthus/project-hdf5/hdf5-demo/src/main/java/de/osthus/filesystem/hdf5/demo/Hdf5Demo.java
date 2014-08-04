package de.osthus.filesystem.hdf5.demo;

import java.nio.file.spi.FileSystemProvider;
import java.util.List;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
public class Hdf5Demo
{
	public static void main(String[] args)
	{
		List<FileSystemProvider> installedProviders = FileSystemProvider.installedProviders();
		for (FileSystemProvider provider : installedProviders)
		{
			System.out.println(provider.getScheme());
		}
	}
}
