package de.osthus.filesystem.directory;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
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
import org.junit.Ignore;
import org.junit.Test;

public class DirectoryFileSystemProviderTest
{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	private DirectoryFileSystemProvider directoryFileSystemProvider;

	@Before
	public void setUp() throws Exception
	{
		directoryFileSystemProvider = new DirectoryFileSystemProvider();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testGetScheme()
	{
		String schema = directoryFileSystemProvider.getScheme();
		assertEquals("dir", schema);
	}

	@Test
	@Ignore
	public void testNewFileSystemURIMapOfStringQ() throws IOException
	{
		URI uri = null;
		Map<String, ?> env = null;
		directoryFileSystemProvider.newFileSystem(uri, env);
	}

	@Test
	@Ignore
	public void testGetFileSystemURI()
	{
		URI uri = null;
		directoryFileSystemProvider.getFileSystem(uri);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewFileChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray() throws IOException
	{
		Path path = null;
		Set<? extends OpenOption> options = null;
		FileAttribute<?> attrs = null;
		directoryFileSystemProvider.newFileChannel(path, options, attrs);
	}

	@Test
	public void testGetPathURI() throws URISyntaxException
	{
		String dirFsUriString = "dir:///file:///C:/temp/target/";
		String folderPathString = "/insideDirFs/folder";
		String fullUriString = dirFsUriString + "#" + folderPathString;
		URI fullUri = new URI(fullUriString);

		DirectoryPath path = directoryFileSystemProvider.getPath(fullUri);

		URI dirFsUri = new URI(dirFsUriString);
		DirectoryFileSystem dirFileSystem = directoryFileSystemProvider.useFileSystem(dirFsUri);
		assertEquals(dirFileSystem, path.fileSystem);

		assertEquals(dirFileSystem.getSeparator(), path.root);
		assertEquals(folderPathString, path.path);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewByteChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray() throws IOException
	{
		Path path = null;
		Set<? extends OpenOption> options = null;
		FileAttribute<?> attrs = null;
		directoryFileSystemProvider.newByteChannel(path, options, attrs);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewDirectoryStreamPathFilterOfQsuperPath() throws IOException
	{
		Path dir = null;
		Filter<? super Path> filter = null;
		directoryFileSystemProvider.newDirectoryStream(dir, filter);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCreateDirectoryPathFileAttributeOfQArray() throws IOException
	{
		Path dir = null;
		FileAttribute<?> attrs = null;
		directoryFileSystemProvider.createDirectory(dir, attrs);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDeletePath() throws IOException
	{
		Path path = null;
		directoryFileSystemProvider.delete(path);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCopyPathPathCopyOptionArray() throws IOException
	{
		Path source = null;
		Path target = null;
		CopyOption options = null;
		directoryFileSystemProvider.copy(source, target, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testMovePathPathCopyOptionArray() throws IOException
	{
		Path source = null;
		Path target = null;
		CopyOption options = null;
		directoryFileSystemProvider.move(source, target, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsSameFilePathPath() throws IOException
	{
		Path path = null;
		Path path2 = null;
		directoryFileSystemProvider.isSameFile(path, path2);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsHiddenPath() throws IOException
	{
		Path path = null;
		directoryFileSystemProvider.isHidden(path);
	}

	@Test
	public void testGetFileStorePath() throws IOException, URISyntaxException
	{
		URI sysUri = new URI(TestConstant.NAME_FILE_FS_C_TEMP);
		Path sysPath = Paths.get(sysUri);

		// TODO Implement getPath to expect full uri, split it to get FS and from that the Path obj
		// Path dirPath = directoryFileSystemProvider.getPath(uri);

		URI dirUri = new URI(TestConstant.NAME_DIR_FS_C_TEMP);
		DirectoryFileSystem fileSystem = directoryFileSystemProvider.newFileSystem(dirUri, Collections.<String, Object> emptyMap());
		Path dirPath = new DirectoryPath(fileSystem, "", "");

		FileStore expected = FileSystems.getDefault().provider().getFileStore(sysPath);
		FileStore actual = directoryFileSystemProvider.getFileStore(dirPath);
		assertEquals(expected, actual);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCheckAccessPathAccessModeArray() throws IOException
	{
		Path path = null;
		AccessMode modes = null;
		directoryFileSystemProvider.checkAccess(path, modes);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetFileAttributeViewPathClassOfVLinkOptionArray()
	{
		Path path = null;
		Class<FileAttributeView> type = null;
		LinkOption options = null;
		directoryFileSystemProvider.getFileAttributeView(path, type, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testReadAttributesPathClassOfALinkOptionArray() throws IOException
	{
		Path path = null;
		Class<? extends BasicFileAttributes> type = null;
		LinkOption[] options = null;
		directoryFileSystemProvider.readAttributes(path, type, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testReadAttributesPathStringLinkOptionArray() throws IOException
	{
		Path path = null;
		String attributes = null;
		LinkOption[] options = null;
		directoryFileSystemProvider.readAttributes(path, attributes, options);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetAttributePathStringObjectLinkOptionArray() throws IOException
	{
		Path path = null;
		String attribute = null;
		Object value = null;
		LinkOption[] options = null;
		directoryFileSystemProvider.setAttribute(path, attribute, value, options);
	}

}
