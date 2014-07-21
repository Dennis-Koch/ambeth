package de.osthus.filesystem.hdf5;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
public final class TestConstant
{
	public static final String NAME_FILE_FS_TEST_FILE;

	public static final String NAME_HDF5_FS_TEST_FILE;

	public static final String NAME_HDF5_FS_TEST_FILE_PATH;

	static
	{
		String testFileName = System.getProperty("java.io.tmpdir") + "test.h5";
		Path testFilePath = Paths.get(testFileName);
		URI testFileUri = testFilePath.toUri();
		NAME_FILE_FS_TEST_FILE = testFileUri.toString();
		NAME_HDF5_FS_TEST_FILE = Hdf5FileSystemProvider.SCHEME + ":" + NAME_FILE_FS_TEST_FILE;
		NAME_HDF5_FS_TEST_FILE_PATH = NAME_HDF5_FS_TEST_FILE + "!/inside";
	}

	private TestConstant()
	{
		// Intended blank
	}
}
