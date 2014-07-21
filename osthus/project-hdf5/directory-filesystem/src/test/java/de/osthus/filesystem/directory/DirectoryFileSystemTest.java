package de.osthus.filesystem.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DirectoryFileSystemTest
{
	private static FileSystem defaultFileSystem;

	private static URI testUri;

	private static DirectoryFileSystemProvider directoryFileSystemProvider;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		defaultFileSystem = FileSystems.getDefault();
		testUri = new URI(TestConstant.NAME_DIR_FS_TEMP_FOLDER);
		directoryFileSystemProvider = new DirectoryFileSystemProvider();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	private DirectoryFileSystem directoryFileSystem;

	@Before
	public void setUp() throws Exception
	{
		directoryFileSystem = directoryFileSystemProvider.newFileSystem(testUri, Collections.<String, Object> emptyMap());
	}

	@After
	public void tearDown() throws Exception
	{
		if (directoryFileSystem != null)
		{
			directoryFileSystem.close();
		}
	}

	@Test
	public void testClose() throws IOException
	{
		assertTrue(directoryFileSystem.isOpen());

		directoryFileSystem.close();
		assertFalse(directoryFileSystem.isOpen());
	}

	@Test
	public void testIsOpen() throws IOException
	{
		assertTrue(directoryFileSystem.isOpen());
		String separator = directoryFileSystem.getSeparator();
		assertNotNull(separator);

		directoryFileSystem.close();
		assertFalse(directoryFileSystem.isOpen());
	}

	@Test(expected = ClosedFileSystemException.class)
	public void testIsOpenCheck() throws IOException
	{
		directoryFileSystem.close();
		directoryFileSystem.getSeparator();
	}

	@Test
	public void testIsReadOnly()
	{
		assertEquals(defaultFileSystem.isReadOnly(), directoryFileSystem.isReadOnly());
	}

	@Test
	public void testProvider()
	{
		FileSystemProvider provider = directoryFileSystem.provider();
		assertSame(directoryFileSystemProvider, provider);
	}

	@Test
	public void testGetSeparator()
	{
		String separator = directoryFileSystem.getSeparator();
		assertEquals("/", separator);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetRootDirectories()
	{
		Iterable<Path> rootDirectories = directoryFileSystem.getRootDirectories();
		assertNotNull(rootDirectories);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetFileStores()
	{
		Iterable<FileStore> fileStores = directoryFileSystem.getFileStores();
		assertNotNull(fileStores);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSupportedFileAttributeViews()
	{
		Set<String> supportedFileAttributeViews = directoryFileSystem.supportedFileAttributeViews();
		assertNotNull(supportedFileAttributeViews);
	}

	@Test
	public void testGetPath_empty()
	{
		String first = "";
		DirectoryPath path = directoryFileSystem.getPath(first);
		assertNotNull(path);
		assertSame(directoryFileSystem, path.getFileSystem());
		assertNull(path.getRoot());
		assertEquals("", path.toString());
	}

	@Test
	public void testGetPath_root()
	{
		String first = "/";
		DirectoryPath path = directoryFileSystem.getPath(first);
		assertNotNull(path);
		assertSame(directoryFileSystem, path.getFileSystem());
		assertEquals("/", path.getRoot().toString());
		assertEquals("/", path.toString());
	}

	@Test
	public void testGetPath_simple()
	{
		String first = "/data";
		DirectoryPath path = directoryFileSystem.getPath(first);
		assertNotNull(path);
		assertSame(directoryFileSystem, path.getFileSystem());
		assertEquals("/", path.getRoot().toString());
		assertEquals("/data", path.toString());
	}

	@Test
	public void testGetPath_concat1()
	{
		String first = "/data";
		String second = "/test/";
		String third = "/dir";
		DirectoryPath path = directoryFileSystem.getPath(first, second, third);
		assertNotNull(path);
		assertSame(directoryFileSystem, path.getFileSystem());
		assertEquals("/", path.getRoot().toString());
		assertEquals("/data/test/dir", path.toString());
	}

	@Test
	public void testGetPath_concat2()
	{
		String first = "/data";
		String second = "\\test\\";
		String third = "/dir";
		DirectoryPath path = directoryFileSystem.getPath(first, second, third);
		assertNotNull(path);
		assertSame(directoryFileSystem, path.getFileSystem());
		assertEquals("/", path.getRoot().toString());
		assertEquals("/data/test/dir", path.toString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetPathMatcherString()
	{
		String syntaxAndPattern = "";
		directoryFileSystem.getPathMatcher(syntaxAndPattern);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetUserPrincipalLookupService()
	{
		directoryFileSystem.getUserPrincipalLookupService();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewWatchService() throws IOException
	{
		directoryFileSystem.newWatchService();
	}

}
