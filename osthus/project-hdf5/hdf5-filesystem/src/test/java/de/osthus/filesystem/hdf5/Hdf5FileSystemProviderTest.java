package de.osthus.filesystem.hdf5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import java.nio.file.Files;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-23
 */
public class Hdf5FileSystemProviderTest
{
	private static final Path TEST_PATH = Paths.get(TestConstant.TEST_FILE_SYS_NAME);

	private Hdf5FileSystemProvider hdf5FileSystemProvider;

	@Before
	public void setUp() throws Exception
	{
		Files.deleteIfExists(TEST_PATH);

		hdf5FileSystemProvider = new Hdf5FileSystemProvider();
	}

	@After
	public void tearDown() throws Exception
	{
		Files.deleteIfExists(TEST_PATH);
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
		URI uri = new URI(TestConstant.EMPTY_FILE_HDF5_URI);
		Map<String, ?> env = Collections.emptyMap();

		Hdf5FileSystem fileSystem = hdf5FileSystemProvider.newFileSystem(uri, env);

		assertNotNull(fileSystem);
		assertEquals(TestConstant.EMPTY_FILE_SYS_URI, fileSystem.getUnderlyingFile().toUri().toString());
	}

	@Test(expected = FileSystemAlreadyExistsException.class)
	public void testNewFileSystemURIMapOfStringQ_existing() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.EMPTY_FILE_HDF5_URI);
		Map<String, ?> env = Collections.emptyMap();
		hdf5FileSystemProvider.newFileSystem(uri, env);

		// second call throws exception
		hdf5FileSystemProvider.newFileSystem(uri, env);
	}

	@Test
	public void testGetFileSystemURI() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.EMPTY_FILE_HDF5_URI);
		Map<String, ?> env = Collections.emptyMap();
		Hdf5FileSystem expected = hdf5FileSystemProvider.newFileSystem(uri, env);

		Hdf5FileSystem actual = hdf5FileSystemProvider.getFileSystem(uri);
		assertEquals(expected, actual);
	}

	@Test(expected = FileSystemNotFoundException.class)
	public void testGetFileSystemURI_notExisting() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.EMPTY_FILE_HDF5_URI);
		hdf5FileSystemProvider.getFileSystem(uri);
	}

	@Test
	@Ignore
	public void testNewFileChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewFileChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray_unsupported() throws IOException
	{
		Path path = null;
		Set<? extends OpenOption> options = null;
		FileAttribute<?> attrs = null;

		hdf5FileSystemProvider.newFileChannel(path, options, attrs);
	}

	@Test
	public void testGetPathURI()
	{
		String fullUriString = TestConstant.TEST_FILE_HDF5_DATA_URI;
		URI fullUri = URI.create(fullUriString);

		Hdf5Path path = hdf5FileSystemProvider.getPath(fullUri);

		Hdf5Uri dirFsUri = Hdf5Uri.create(fullUriString);
		Hdf5FileSystem dirFileSystem = hdf5FileSystemProvider.useFileSystem(dirFsUri);
		assertEquals(dirFileSystem, path.getFileSystem());

		assertEquals(dirFileSystem.getSeparator(), path.getRoot().toString());
		assertEquals(TestConstant.DATA_FOLDER, path.toString());
	}

	@Test
	@Ignore
	public void testNewByteChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewByteChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray_unsupported() throws IOException
	{
		Path path = null;
		Set<? extends OpenOption> options = null;
		FileAttribute<?> attrs = null;

		hdf5FileSystemProvider.newByteChannel(path, options, attrs);
	}

	@Test
	@Ignore
	public void testNewDirectoryStreamPathFilterOfQsuperPath() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewDirectoryStreamPathFilterOfQsuperPath_unsupported() throws IOException
	{
		Path dir = null;
		Filter<? super Path> filter = null;

		hdf5FileSystemProvider.newDirectoryStream(dir, filter);
	}

	@Test
	public void testCreateDirectoryPathFileAttributeOfQArray_simple() throws IOException
	{
		URI uri = URI.create(TestConstant.TEST_FILE_HDF5_DATA_URI);
		Hdf5Path dir = hdf5FileSystemProvider.getPath(uri);
		hdf5FileSystemProvider.createDirectory(dir);
	}

	@Test
	@Ignore
	public void testCreateDirectoryPathFileAttributeOfQArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCreateDirectoryPathFileAttributeOfQArray_unsupported() throws IOException
	{
		Path dir = null;
		FileAttribute<?> attrs = null;

		hdf5FileSystemProvider.createDirectory(dir, attrs);
	}

	@Test
	@Ignore
	public void testDeletePath() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDeletePath_unsupported() throws IOException
	{
		Path path = null;
		hdf5FileSystemProvider.delete(path);
	}

	@Test
	@Ignore
	public void testCopyPathPathCopyOptionArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCopyPathPathCopyOptionArray_unsupported() throws IOException
	{
		Path source = null;
		Path target = null;
		CopyOption options = null;

		hdf5FileSystemProvider.copy(source, target, options);
	}

	@Test
	@Ignore
	public void testMovePathPathCopyOptionArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testMovePathPathCopyOptionArray_unsupported() throws IOException
	{
		Path source = null;
		Path target = null;
		CopyOption options = null;
		hdf5FileSystemProvider.move(source, target, options);
	}

	@Test
	@Ignore
	public void testIsSameFilePathPath() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsSameFilePathPath_unsupported() throws IOException
	{
		Path path = null;
		Path path2 = null;
		hdf5FileSystemProvider.isSameFile(path, path2);
	}

	@Test
	@Ignore
	public void testIsHiddenPath() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsHiddenPath_unsupported() throws IOException
	{
		Path path = null;
		hdf5FileSystemProvider.isHidden(path);
	}

	@Test
	public void testGetFileStorePath() throws IOException, URISyntaxException
	{
		Path sysPath = TEST_PATH;

		URI dirUri = new URI(TestConstant.TEST_FILE_HDF5_DATA_URI);
		Path dirPath = hdf5FileSystemProvider.getPath(dirUri);

		FileStore expected = FileSystems.getDefault().provider().getFileStore(sysPath);
		FileStore actual = hdf5FileSystemProvider.getFileStore(dirPath);
		assertEquals(expected, actual);
	}

	@Test
	@Ignore
	public void testCheckAccessPathAccessModeArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCheckAccessPathAccessModeArray_unsupported() throws IOException
	{
		Path path = null;
		AccessMode modes = null;

		hdf5FileSystemProvider.checkAccess(path, modes);
	}

	@Test
	@Ignore
	public void testGetFileAttributeViewPathClassOfVLinkOptionArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetFileAttributeViewPathClassOfVLinkOptionArray_unsupported()
	{
		Path path = null;
		Class<FileAttributeView> type = null;
		LinkOption options = null;

		hdf5FileSystemProvider.getFileAttributeView(path, type, options);
	}

	@Test
	@Ignore
	public void testReadAttributesPathClassOfALinkOptionArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testReadAttributesPathClassOfALinkOptionArray_unsupported() throws IOException
	{
		Path path = null;
		Class<? extends BasicFileAttributes> type = null;
		LinkOption[] options = null;

		hdf5FileSystemProvider.readAttributes(path, type, options);
	}

	@Test
	@Ignore
	public void testReadAttributesPathStringLinkOptionArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testReadAttributesPathStringLinkOptionArray_unsupported() throws IOException
	{
		Path path = null;
		String attributes = null;
		LinkOption[] options = null;

		hdf5FileSystemProvider.readAttributes(path, attributes, options);
	}

	@Test
	@Ignore
	public void testSetAttributePathStringObjectLinkOptionArray() throws IOException
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetAttributePathStringObjectLinkOptionArray_unsupported() throws IOException
	{
		Path path = null;
		String attribute = null;
		Object value = null;
		LinkOption[] options = null;

		hdf5FileSystemProvider.setAttribute(path, attribute, value, options);
	}

	@Test
	public void testCreateHdf5FilePath() throws IOException
	{
		assertFalse(Files.exists(TEST_PATH));
		hdf5FileSystemProvider.createHdf5File(TEST_PATH);
		assertTrue(Files.exists(TEST_PATH));
	}
}
