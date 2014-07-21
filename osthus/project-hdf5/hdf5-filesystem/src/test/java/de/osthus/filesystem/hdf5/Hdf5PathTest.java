package de.osthus.filesystem.hdf5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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
 * @start 2014-07-21
 */
// TODO Change for HDF5
@Ignore
public class Hdf5PathTest
{
	private static Hdf5FileSystemProvider DIRECTORY_FILE_SYSTEM_PROVIDER;

	private static Hdf5FileSystem DIRECTORY_FILE_SYSTEM;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		DIRECTORY_FILE_SYSTEM_PROVIDER = new Hdf5FileSystemProvider();
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
		Hdf5Path directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/");
		assertTrue(directoryPath.isAbsolute());

		directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/test");
		assertTrue(directoryPath.isAbsolute());

		directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "test");
		assertFalse(directoryPath.isAbsolute());
	}

	@Test
	public void testGetFileName()
	{
		Hdf5Path directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/");
		Hdf5Path fileName = directoryPath.getFileName();
		assertNull(fileName);

		directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/test");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test", fileName.toString());

		directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "test");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test", fileName.toString());

		directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/test/test2/test3");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test3", fileName.toString());
	}

	@Test
	public void testGetParent()
	{
		Hdf5Path root = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/");
		// TODO replace with resolve() when implemented
		Hdf5Path path = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", root.toString() + "tmp");

		Hdf5Path parent = path.getParent();
		assertNotNull(parent);
		assertEquals(root, parent);
	}

	@Test
	public void testGetParent_relativeSingleName()
	{
		Hdf5Path path = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "", "tmp");

		Hdf5Path parent = path.getParent();
		assertNull(parent);
	}

	@Test
	public void testGetParent_relativeNames()
	{
		Hdf5Path expected = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "", "data");
		// TODO replace with resolve() when implemented
		Hdf5Path path = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "", expected.toString() + "/tmp");

		Hdf5Path parent = path.getParent();
		assertNotNull(parent);
		assertEquals(expected, parent);
	}

	@Test
	public void testGetParent_relativeNamesDir()
	{
		Hdf5Path expected = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "", "data");
		// TODO replace with resolve() when implemented
		Hdf5Path path = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "", expected.toString() + "/tmp/");

		Hdf5Path parent = path.getParent();
		assertNotNull(parent);
		assertEquals(expected, parent);
	}

	@Test
	public void testGetParent_ofRoot()
	{
		Hdf5Path root = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/");

		Hdf5Path parent = root.getParent();
		assertNull(parent);
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
	public void testResolvePath_otherEmpty()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("");

		Path resolved = path1.resolve(path2);
		assertSame(path1, resolved);
	}

	@Test
	public void testResolvePath_bothAbsolute()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("/test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("/test2");

		Path resolved = path1.resolve(path2);
		assertSame(path2, resolved);
	}

	@Test
	public void testResolvePath_thisRelative()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("/test2");

		Path resolved = path1.resolve(path2);
		assertSame(path2, resolved);
	}

	@Test
	public void testResolvePath_otherRelative()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("/test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("test2");

		Path resolved = path1.resolve(path2);
		assertEquals(path1.toString() + "/" + path2.toString(), resolved.toString());
	}

	@Test
	public void testResolvePath_bothRelative()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("test2");

		Path resolved = path1.resolve(path2);
		assertEquals(path1.toString() + "/" + path2.toString(), resolved.toString());
	}

	@Test
	public void testResolveString_otherEmpty()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("");

		Path resolved = path1.resolve(path2);
		assertSame(path1, resolved);
	}

	@Test
	public void testResolveString_bothAbsolute()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("/test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("/test2");

		Path resolved = path1.resolve(path2.toString());
		assertEquals(path2, resolved);
	}

	@Test
	public void testResolveString_thisRelative()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("/test2");

		Path resolved = path1.resolve(path2.toString());
		assertEquals(path2, resolved);
	}

	@Test
	public void testResolveString_otherRelative()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("/test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("test2");

		Path resolved = path1.resolve(path2.toString());
		assertEquals(path1.toString() + "/" + path2.toString(), resolved.toString());
	}

	@Test
	public void testResolveString_bothRelative()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("test2");

		Path resolved = path1.resolve(path2.toString());
		assertEquals(path1.toString() + "/" + path2.toString(), resolved.toString());
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
	public void testRelativize_simple()
	{
		Hdf5Path path1 = DIRECTORY_FILE_SYSTEM.getPath("/");
		Hdf5Path path2 = DIRECTORY_FILE_SYSTEM.getPath("/test");

		Path relativized = path1.relativize(path2);
		assertEquals("test", relativized.toString());
	}

	@Test
	@Ignore
	public void testRelativize_todo()
	{
		// TODO
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
		Hdf5Path directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/test");
		Path root = directoryPath.getRoot();
		assertNotNull(root);
		assertEquals(root, root.getRoot());
		assertNull(root.getFileName());
		assertNull(root.getParent());

		directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "/", "/test/test2/test3");
		root = directoryPath.getRoot();
		assertNotNull(root);
		assertEquals(root, root.getRoot());
		assertNull(root.getFileName());
		assertNull(root.getParent());

		directoryPath = new Hdf5Path(DIRECTORY_FILE_SYSTEM, "", "test/test2/test3");
		root = directoryPath.getRoot();
		assertNull(root);
	}
}
