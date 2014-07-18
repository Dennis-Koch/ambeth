package de.osthus.filesystem.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public void testNewFileSystemURIMapOfStringQ() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.NAME_DIR_FS_TEMP_FOLDER);
		Map<String, ?> env = Collections.emptyMap();
		DirectoryFileSystem fileSystem = directoryFileSystemProvider.newFileSystem(uri, env);
		assertNotNull(fileSystem);
		assertEquals(TestConstant.NAME_FILE_FS_TEMP_FOLDER, fileSystem.getUnderlyingFileSystemPath().toUri().toString());
	}

	@Test(expected = FileSystemAlreadyExistsException.class)
	public void testNewFileSystemURIMapOfStringQ_existing() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.NAME_DIR_FS_TEMP_FOLDER);
		Map<String, ?> env = Collections.emptyMap();
		directoryFileSystemProvider.newFileSystem(uri, env);

		// second call throws exception
		directoryFileSystemProvider.newFileSystem(uri, env);
	}

	@Test
	public void testGetFileSystemURI() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.NAME_DIR_FS_TEMP_FOLDER);
		Map<String, ?> env = Collections.emptyMap();
		DirectoryFileSystem expected = directoryFileSystemProvider.newFileSystem(uri, env);

		DirectoryFileSystem actual = directoryFileSystemProvider.getFileSystem(uri);
		assertEquals(expected, actual);
	}

	@Test(expected = FileSystemNotFoundException.class)
	public void testGetFileSystemURI_notExisting() throws IOException, URISyntaxException
	{
		URI uri = new URI(TestConstant.NAME_DIR_FS_TEMP_FOLDER);
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
		String dirFsUriString = TestConstant.NAME_DIR_FS_TEMP_FOLDER;
		String folderPathString = "/insideDirFs/folder";
		String fullUriString = dirFsUriString + "!" + folderPathString;
		URI fullUri = new URI(fullUriString);

		DirectoryPath path = directoryFileSystemProvider.getPath(fullUri);

		URI dirFsUri = new URI(dirFsUriString);
		DirectoryFileSystem dirFileSystem = directoryFileSystemProvider.useFileSystem(dirFsUri);
		assertEquals(dirFileSystem, path.fileSystem);

		assertEquals(dirFileSystem.getSeparator(), path.root);
		assertEquals(folderPathString, path.path);
	}

	// TODO The usage of folders inside a zip file is currently not supported due to problems with parsing the URI
	// @Test
	// public void testGetPathURI_inZip() throws URISyntaxException
	// {
	// String zipFsUriString = "jar:" + TestConstant.NAME_FILE_FS_TEMP_FOLDER + "test.zip!/insideZipFs/";
	// String dirFsUriString = "dir:" + zipFsUriString;
	// String folderPathString = "/insideDirFs/folder";
	// String fullUriString = dirFsUriString + "!" + folderPathString;
	// URI fullUri = new URI(fullUriString);
	//
	// DirectoryPath path = directoryFileSystemProvider.getPath(fullUri);
	//
	// URI dirFsUri = new URI(dirFsUriString);
	// DirectoryFileSystem dirFileSystem = directoryFileSystemProvider.useFileSystem(dirFsUri);
	// assertEquals(dirFileSystem, path.fileSystem);
	//
	// assertEquals(dirFileSystem.getSeparator(), path.root);
	// assertEquals(folderPathString, path.path);
	// }

	@Test
	@Ignore
	public void testNewByteChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray() throws IOException
	{
		Path path = null;
		Set<? extends OpenOption> options = null;
		FileAttribute<?> attrs = null;
		directoryFileSystemProvider.newByteChannel(path, options, attrs);
	}

	@Test
	@Ignore
	public void testNewDirectoryStreamPathFilterOfQsuperPath() throws IOException
	{
		Path dir = null;
		Filter<? super Path> filter = null;
		directoryFileSystemProvider.newDirectoryStream(dir, filter);
	}

	@Test
	@Ignore
	public void testCreateDirectoryPathFileAttributeOfQArray() throws IOException
	{
		// TODO
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
		URI sysUri = new URI(TestConstant.NAME_FILE_FS_TEMP_FOLDER);
		Path sysPath = Paths.get(sysUri);

		URI dirUri = new URI(TestConstant.NAME_DIR_FS_TEMP_FOLDER_PATH);
		Path dirPath = directoryFileSystemProvider.getPath(dirUri);

		FileStore expected = FileSystems.getDefault().provider().getFileStore(sysPath);
		FileStore actual = directoryFileSystemProvider.getFileStore(dirPath);
		assertEquals(expected, actual);
	}

	@Test
	@Ignore
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

	@Test
	@Ignore
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

	@Test
	public void testFsPattern_defaultWindows()
	{
		String uriString = "dir:file:///C:/temp/target/";
		Matcher matcher = patternHelper(DirectoryFileSystemProvider.FS_URI_PATTERN, uriString);
		assertEquals(5, matcher.groupCount());
		assertEquals(uriString, matcher.group(0));
		assertEquals("file:///C:/temp/target/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_IDENTIFIER));
		assertEquals("file:", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_SCHEME));
		assertEquals("//", matcher.group(3));
		assertEquals("/C:/temp/target/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_PATH));
		assertEquals("C:/temp/target/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_PATH_2));
	}

	@Test
	public void testFsPattern_shortenedWindows()
	{
		String uriString = "dir:file:/C:/temp/target/";
		Matcher matcher = patternHelper(DirectoryFileSystemProvider.FS_URI_PATTERN, uriString);
		assertEquals(5, matcher.groupCount());
		assertEquals(uriString, matcher.group(0));
		assertEquals("file:/C:/temp/target/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_IDENTIFIER));
		assertEquals("file:", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_SCHEME));
		assertNull(matcher.group(3));
		assertEquals("/C:/temp/target/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_PATH));
		assertEquals("C:/temp/target/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_PATH_2));
	}

	// TODO The usage of folders inside a zip file is currently not supported due to problems with parsing the URI
	// @Test
	// public void testFsPattern_inZipFile()
	// {
	// String uriString = "dir:jar:file:///C:/temp/target/test.zip!/test/";
	// Matcher matcher = patternHelper(DirectoryFileSystemProvider.FS_URI_PATTERN, uriString);
	// assertEquals(5, matcher.groupCount());
	// assertEquals(uriString, matcher.group(0));
	// assertEquals("jar:file:///C:/temp/target/test.zip!/test/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_IDENTIFIER));
	// assertEquals("jar:file:///C:/temp/target/test.zip", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_SCHEME));
	// assertEquals("//", matcher.group(3));
	// assertEquals("/test/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_PATH));
	// assertEquals("test/", matcher.group(DirectoryFileSystemProvider.FS_URI_GROUP_SUB_PATH_2));
	// }

	@Test
	public void testPathPattern_defaultWindows()
	{
		String uriString = "dir:file:///C:/temp/target/!/insideDirFs/folder";
		Matcher matcher = patternHelper(DirectoryFileSystemProvider.PATH_URI_PATTERN, uriString);
		assertEquals(3, matcher.groupCount());
		assertEquals(uriString, matcher.group(0));
		assertEquals("dir:file:///C:/temp/target/", matcher.group(DirectoryFileSystemProvider.PATH_URI_GROUP_FS_URI));
		assertEquals("//", matcher.group(2));
		assertEquals("/insideDirFs/folder", matcher.group(DirectoryFileSystemProvider.PATH_URI_GROUP_PATH));
	}

	@Test
	public void testPathPattern_inZipFile()
	{
		String uriString = "dir:jar:file:///C:/temp/target/test.zip!/test/!/insideDirFs/folder";
		Matcher matcher = patternHelper(DirectoryFileSystemProvider.PATH_URI_PATTERN, uriString);
		assertEquals(3, matcher.groupCount());
		assertEquals(uriString, matcher.group(0));
		assertEquals("dir:jar:file:///C:/temp/target/test.zip!/test/", matcher.group(DirectoryFileSystemProvider.PATH_URI_GROUP_FS_URI));
		assertEquals("//", matcher.group(2));
		assertEquals("/insideDirFs/folder", matcher.group(DirectoryFileSystemProvider.PATH_URI_GROUP_PATH));
	}

	private Matcher patternHelper(Pattern pattern, String string)
	{
		Matcher matcher = pattern.matcher(string);
		assertTrue(pattern.pattern() + " => " + string, matcher.matches());
		return matcher;
	}
}
