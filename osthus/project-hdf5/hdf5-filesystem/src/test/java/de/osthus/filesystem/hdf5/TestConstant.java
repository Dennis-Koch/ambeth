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
	public static final String EMPTY_FILE_SYS_NAME;

	public static final String EMPTY_FILE_SYS_URI;

	public static final String EMPTY_FILE_HDF5_URI;

	public static final String EMPTY_FILE_HDF5_DATA_URI;

	public static final String TEST_FILE_SYS_NAME;

	public static final String TEST_FILE_SYS_URI;

	public static final String TEST_FILE_HDF5_URI;

	public static final String TEST_FILE_HDF5_DATA_URI;

	public static final String DATA_FOLDER = "/data";

	static
	{
		String testFileName = "src/test/resources/empty.h5";
		Path testFilePath = Paths.get(testFileName);
		EMPTY_FILE_SYS_NAME = testFilePath.toString();
		URI testFileUri = testFilePath.toUri();
		EMPTY_FILE_SYS_URI = testFileUri.toString();
		EMPTY_FILE_HDF5_URI = Hdf5FileSystemProvider.SCHEME + ":" + EMPTY_FILE_SYS_URI;
		EMPTY_FILE_HDF5_DATA_URI = EMPTY_FILE_HDF5_URI + "!" + DATA_FOLDER;

		testFileName = "src/test/resources/test.h5";
		testFilePath = Paths.get(testFileName);
		TEST_FILE_SYS_NAME = testFilePath.toString();
		testFileUri = testFilePath.toUri();
		TEST_FILE_SYS_URI = testFileUri.toString();
		TEST_FILE_HDF5_URI = Hdf5FileSystemProvider.SCHEME + ":" + TEST_FILE_SYS_URI;
		TEST_FILE_HDF5_DATA_URI = TEST_FILE_HDF5_URI + "!" + DATA_FOLDER;
	}

	private TestConstant()
	{
		// Intended blank
	}
}
