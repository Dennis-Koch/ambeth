package de.osthus.filesystem.hdf5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-23
 */
public class Hdf5FileSystemProviderTest
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	private Hdf5FileSystemProvider hdf5FileSystemProvider;

	@Before
	public void setUp() throws Exception
	{
		hdf5FileSystemProvider = new Hdf5FileSystemProvider();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testGetScheme()
	{
		String schema = hdf5FileSystemProvider.getScheme();
		assertEquals("hdf5", schema);
	}

	@Test
	public void testNewFileSystemURIMapOfStringQ() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.NAME_HDF5_FS_TEST_FILE);
		Map<String, ?> env = Collections.emptyMap();

		Hdf5FileSystem fileSystem = hdf5FileSystemProvider.newFileSystem(uri, env);

		assertNotNull(fileSystem);
		assertEquals(TestConstant.NAME_FILE_FS_TEST_FILE, fileSystem.getUnderlyingFile().toUri().toString());
	}

	@Test(expected = FileSystemAlreadyExistsException.class)
	public void testNewFileSystemURIMapOfStringQ_existing() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.NAME_HDF5_FS_TEST_FILE);
		Map<String, ?> env = Collections.emptyMap();
		hdf5FileSystemProvider.newFileSystem(uri, env);

		// second call throws exception
		hdf5FileSystemProvider.newFileSystem(uri, env);
	}

	@Test
	public void testGetFileSystemURI() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.NAME_HDF5_FS_TEST_FILE);
		Map<String, ?> env = Collections.emptyMap();
		Hdf5FileSystem expected = hdf5FileSystemProvider.newFileSystem(uri, env);

		Hdf5FileSystem actual = hdf5FileSystemProvider.getFileSystem(uri);
		assertEquals(expected, actual);
	}

	@Test(expected = FileSystemNotFoundException.class)
	public void testGetFileSystemURI_notExisting() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.NAME_HDF5_FS_TEST_FILE);
		hdf5FileSystemProvider.getFileSystem(uri);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewFileChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray() throws IOException
	{
		Path path = null;
		Set<? extends OpenOption> options = null;
		FileAttribute<?> attrs = null;

		hdf5FileSystemProvider.newFileChannel(path, options, attrs);
	}

	@Test
	public void testGetPathURI()
	{
		String dirFsUriString = TestConstant.NAME_HDF5_FS_TEST_FILE;
		String folderPathString = "/insideDirFs/folder";
		String fullUriString = dirFsUriString + "!" + folderPathString;
		URI fullUri = URI.create(fullUriString);

		Hdf5Path path = hdf5FileSystemProvider.getPath(fullUri);

		Hdf5Uri dirFsUri = Hdf5Uri.create(dirFsUriString);
		Hdf5FileSystem dirFileSystem = hdf5FileSystemProvider.useFileSystem(dirFsUri);
		assertEquals(dirFileSystem, path.getFileSystem());

		assertEquals(dirFileSystem.getSeparator(), path.getRoot().toString());
		assertEquals(folderPathString, path.toString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewByteChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray() throws IOException
	{
		Path path = null;
		Set<? extends OpenOption> options = null;
		FileAttribute<?> attrs = null;

		hdf5FileSystemProvider.newByteChannel(path, options, attrs);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewDirectoryStreamPathFilterOfQsuperPath() throws IOException
	{
		Path dir = null;
		Filter<? super Path> filter = null;

		hdf5FileSystemProvider.newDirectoryStream(dir, filter);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCreateDirectoryPathFileAttributeOfQArray() throws IOException
	{
		Path dir = null;
		FileAttribute<?> attrs = null;

		hdf5FileSystemProvider.createDirectory(dir, attrs);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDeletePath() throws IOException
	{
		Path path = null;
		hdf5FileSystemProvider.delete(path);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCopyPathPathCopyOptionArray() throws IOException
	{
		Path source = null;
		Path target = null;
		CopyOption options = null;

		hdf5FileSystemProvider.copy(source, target, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testMovePathPathCopyOptionArray() throws IOException
	{
		Path source = null;
		Path target = null;
		CopyOption options = null;
		hdf5FileSystemProvider.move(source, target, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsSameFilePathPath() throws IOException
	{
		Path path = null;
		Path path2 = null;
		hdf5FileSystemProvider.isSameFile(path, path2);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsHiddenPath() throws IOException
	{
		Path path = null;
		hdf5FileSystemProvider.isHidden(path);
	}

	@Test
	public void testGetFileStorePath() throws IOException, URISyntaxException
	{
		URI sysUri = new URI(TestConstant.NAME_FILE_FS_TEST_FILE);
		Path sysPath = Paths.get(sysUri);

		URI dirUri = new URI(TestConstant.NAME_HDF5_FS_TEST_FILE_PATH);
		Path dirPath = hdf5FileSystemProvider.getPath(dirUri);

		FileStore expected = FileSystems.getDefault().provider().getFileStore(sysPath);
		FileStore actual = hdf5FileSystemProvider.getFileStore(dirPath);
		assertEquals(expected, actual);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCheckAccessPathAccessModeArray() throws IOException
	{
		Path path = null;
		AccessMode modes = null;

		hdf5FileSystemProvider.checkAccess(path, modes);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetFileAttributeViewPathClassOfVLinkOptionArray()
	{
		Path path = null;
		Class<FileAttributeView> type = null;
		LinkOption options = null;

		hdf5FileSystemProvider.getFileAttributeView(path, type, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testReadAttributesPathClassOfALinkOptionArray() throws IOException
	{
		Path path = null;
		Class<? extends BasicFileAttributes> type = null;
		LinkOption[] options = null;

		hdf5FileSystemProvider.readAttributes(path, type, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testReadAttributesPathStringLinkOptionArray() throws IOException
	{
		Path path = null;
		String attributes = null;
		LinkOption[] options = null;

		hdf5FileSystemProvider.readAttributes(path, attributes, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetAttributePathStringObjectLinkOptionArray() throws IOException
	{
		Path path = null;
		String attribute = null;
		Object value = null;
		LinkOption[] options = null;

		hdf5FileSystemProvider.setAttribute(path, attribute, value, options);
	}
}
