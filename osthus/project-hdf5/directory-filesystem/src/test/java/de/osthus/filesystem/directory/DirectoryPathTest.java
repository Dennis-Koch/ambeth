package de.osthus.filesystem.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.nio.file.Path;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-16
 */
public class DirectoryPathTest
{
	private static DirectoryFileSystemProvider DIRECTORY_FILE_SYSTEM_PROVIDER;

	private static DirectoryFileSystem DIRECTORY_FILE_SYSTEM;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		DIRECTORY_FILE_SYSTEM_PROVIDER = new DirectoryFileSystemProvider();
		URI uri = new URI(TestConstant.NAME_DIR_FS_TEMP_FOLDER);
		DIRECTORY_FILE_SYSTEM = DIRECTORY_FILE_SYSTEM_PROVIDER.newFileSystem(uri, null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testIsAbsolute()
	{
		DirectoryPath directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		assertTrue(directoryPath.isAbsolute());

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test");
		assertTrue(directoryPath.isAbsolute());

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "test");
		assertFalse(directoryPath.isAbsolute());
	}

	@Test
	public void testGetFileName()
	{
		DirectoryPath directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		DirectoryPath fileName = directoryPath.getFileName();
		assertNull(fileName);

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test", fileName.path);

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "test");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test", fileName.path);

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test/test2/test3");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test3", fileName.path);
	}

	@Test
	@Ignore
	public void testGetParent()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetNameCount()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetName()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testSubpath()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testStartsWithPath()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testStartsWithString()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testEndsWithPath()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testEndsWithString()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testNormalize()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testResolvePath()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testResolveString()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testResolveSiblingPath()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testResolveSiblingString()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testRelativize()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testToUri()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testToAbsolutePath()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testToRealPath()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testToFile()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testRegisterWatchServiceKindOfQArrayModifierArray()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testRegisterWatchServiceKindOfQArray()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testIterator()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testCompareTo()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testGetFileSystem()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testGetRoot()
	{
		DirectoryPath directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test");
		Path root = directoryPath.getRoot();
		assertNotNull(root);
		assertEquals(root, root.getRoot());
		assertNull(root.getFileName());
		assertNull(root.getParent());

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test/test2/test3");
		root = directoryPath.getRoot();
		assertNotNull(root);
		assertEquals(root, root.getRoot());
		assertNull(root.getFileName());
		assertNull(root.getParent());

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "", "test/test2/test3");
		root = directoryPath.getRoot();
		assertNull(root);
	}
}
