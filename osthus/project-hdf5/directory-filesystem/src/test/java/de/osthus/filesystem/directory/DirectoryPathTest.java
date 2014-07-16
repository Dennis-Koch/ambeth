package de.osthus.filesystem.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
		URI uri = new URI(TestConstant.DIR_FS_NAME_C_TEMP);
		DIRECTORY_FILE_SYSTEM = (DirectoryFileSystem) DIRECTORY_FILE_SYSTEM_PROVIDER.newFileSystem(uri, null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
		Path path = Paths.get("\\test");
		Path fileName = path.getFileName();
		int nameCount = path.getNameCount();
		Path parent = path.getParent();
		Path root = path.getRoot();

		path = Paths.get("test");
		nameCount = path.getNameCount();
		parent = path.getParent();
		root = path.getRoot();

		path = Paths.get("/test/test2/test2");
		nameCount = path.getNameCount();
		parent = path.getParent();
		root = path.getRoot();

		path = Paths.get("test/test2/test2");
		nameCount = path.getNameCount();
		parent = path.getParent();
		root = path.getRoot();

		path = Paths.get("c:\\test\\test2\\test2");
		nameCount = path.getNameCount();
		parent = path.getParent();
		root = path.getRoot();

		System.out.println();
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
		Path fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("", fileName);

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test", fileName);

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "test");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test", fileName);
	}

	@Test
	public void testGetParent()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testGetNameCount()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testGetName()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testSubpath()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testStartsWithPath()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testStartsWithString()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testEndsWithPath()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testEndsWithString()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testNormalize()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testResolvePath()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testResolveString()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testResolveSiblingPath()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testResolveSiblingString()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testRelativize()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testToUri()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testToAbsolutePath()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testToRealPath()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testToFile()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testRegisterWatchServiceKindOfQArrayModifierArray()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testRegisterWatchServiceKindOfQArray()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testIterator()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testCompareTo()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testGetFileSystem()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testGetRoot()
	{
		fail("Not yet implemented");
	}
}
