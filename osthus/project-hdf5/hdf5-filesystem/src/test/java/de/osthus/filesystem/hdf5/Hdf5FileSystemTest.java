package de.osthus.filesystem.hdf5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-23
 */
public class Hdf5FileSystemTest
{
	private static FileSystem defaultFileSystem;

	private static URI testUri;

	private static Hdf5FileSystemProvider hdf5FileSystemProvider;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		defaultFileSystem = FileSystems.getDefault();
		testUri = new URI(TestConstant.NAME_HDF5_FS_TEST_FILE);
		hdf5FileSystemProvider = new Hdf5FileSystemProvider();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	private Hdf5FileSystem hdf5FileSystem;

	@Before
	public void setUp() throws Exception
	{
		hdf5FileSystem = hdf5FileSystemProvider.newFileSystem(testUri, Collections.<String, Object> emptyMap());
	}

	@After
	public void tearDown() throws Exception
	{
		if (hdf5FileSystem != null)
		{
			hdf5FileSystem.close();
		}
	}

	@Test
	public void testClose() throws IOException
	{
		assertTrue(hdf5FileSystem.isOpen());

		hdf5FileSystem.close();
		assertFalse(hdf5FileSystem.isOpen());

		hdf5FileSystem.close();
	}

	@Test
	public void testIsOpen() throws IOException
	{
		assertTrue(hdf5FileSystem.isOpen());
		String separator = hdf5FileSystem.getSeparator();
		assertNotNull(separator);

		hdf5FileSystem.close();
		assertFalse(hdf5FileSystem.isOpen());
	}

	@Test(expected = ClosedFileSystemException.class)
	public void testIsOpenCheck() throws IOException
	{
		hdf5FileSystem.close();
		hdf5FileSystem.getSeparator();
	}

	@Test
	public void testIsReadOnly()
	{
		assertEquals(defaultFileSystem.isReadOnly(), hdf5FileSystem.isReadOnly());
	}

	@Test
	public void testProvider()
	{
		FileSystemProvider provider = hdf5FileSystem.provider();
		assertSame(hdf5FileSystemProvider, provider);
	}

	@Test
	public void testGetSeparator()
	{
		String separator = hdf5FileSystem.getSeparator();
		assertEquals("/", separator);
	}

	@Test
	public void testGetRootDirectories()
	{
		Iterable<Path> rootDirectories = hdf5FileSystem.getRootDirectories();
		assertNotNull(rootDirectories);

		Iterator<Path> iter = rootDirectories.iterator();
		assertTrue(iter.hasNext());

		Path expected = hdf5FileSystem.getPath("/");
		Path actual = iter.next();
		assertEquals(expected, actual);

		assertFalse(iter.hasNext());
	}

	@Test
	@Ignore
	public void testGetFileStores()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetFileStores_unsupported()
	{
		Iterable<FileStore> fileStores = hdf5FileSystem.getFileStores();
		assertNotNull(fileStores);
	}

	@Test
	@Ignore
	public void testSupportedFileAttributeViews()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSupportedFileAttributeViews_unsupported()
	{
		Set<String> supportedFileAttributeViews = hdf5FileSystem.supportedFileAttributeViews();
		assertNotNull(supportedFileAttributeViews);
	}

	@Test
	public void testGetPath_empty()
	{
		String first = "";
		Hdf5Path path = hdf5FileSystem.getPath(first);
		assertNotNull(path);
		assertSame(hdf5FileSystem, path.getFileSystem());
		assertNull(path.getRoot());
		assertEquals("", path.toString());
	}

	@Test
	public void testGetPath_root()
	{
		String first = "/";
		Hdf5Path path = hdf5FileSystem.getPath(first);
		assertNotNull(path);
		assertSame(hdf5FileSystem, path.getFileSystem());
		assertEquals("/", path.getRoot().toString());
		assertEquals("/", path.toString());
	}

	@Test
	public void testGetPath_simple()
	{
		String first = "/data";
		Hdf5Path path = hdf5FileSystem.getPath(first);
		assertNotNull(path);
		assertSame(hdf5FileSystem, path.getFileSystem());
		assertEquals("/", path.getRoot().toString());
		assertEquals("/data", path.toString());
	}

	@Test
	public void testGetPath_concat1()
	{
		String first = "/data";
		String second = "/test/";
		String third = "/dir";
		Hdf5Path path = hdf5FileSystem.getPath(first, second, third);
		assertNotNull(path);
		assertSame(hdf5FileSystem, path.getFileSystem());
		assertEquals("/", path.getRoot().toString());
		assertEquals("/data/test/dir", path.toString());
	}

	@Test
	public void testGetPath_concat2()
	{
		String first = "/data";
		String second = "\\test\\";
		String third = "/dir";
		Hdf5Path path = hdf5FileSystem.getPath(first, second, third);
		assertNotNull(path);
		assertSame(hdf5FileSystem, path.getFileSystem());
		assertEquals("/", path.getRoot().toString());
		assertEquals("/data/test/dir", path.toString());
	}

	@Test
	@Ignore
	public void testGetPathMatcherString()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetPathMatcherString_unsupported()
	{
		String syntaxAndPattern = "";
		hdf5FileSystem.getPathMatcher(syntaxAndPattern);
	}

	@Test
	@Ignore
	public void testGetUserPrincipalLookupService()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetUserPrincipalLookupService_unsupported()
	{
		hdf5FileSystem.getUserPrincipalLookupService();
	}

	@Test
	@Ignore
	public void testNewWatchService()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNewWatchService_unsupported() throws IOException
	{
		hdf5FileSystem.newWatchService();
	}
}
