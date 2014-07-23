package de.osthus.filesystem.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;

import org.junit.AfterClass;
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
	private static DirectoryFileSystem DIRECTORY_FILE_SYSTEM;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		DirectoryFileSystemProvider directoryFileSystemProvider = new DirectoryFileSystemProvider();
		URI uri = new URI(TestConstant.NAME_DIR_FS_TEMP_FOLDER);
		DIRECTORY_FILE_SYSTEM = directoryFileSystemProvider.newFileSystem(uri, null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		DIRECTORY_FILE_SYSTEM.close();
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
		assertEquals("test", fileName.toString());

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "test");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test", fileName.toString());

		directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test/test2/test3");
		fileName = directoryPath.getFileName();
		assertNotNull(fileName);
		assertEquals("test3", fileName.toString());
	}

	@Test
	public void testGetParent()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		Path path = root.resolve("tmp");

		Path parent = path.getParent();
		assertNotNull(parent);
		assertEquals(root, parent);
	}

	@Test
	public void testGetParent_relativeSingleName()
	{
		DirectoryPath path = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "", "tmp");

		DirectoryPath parent = path.getParent();
		assertNull(parent);
	}

	@Test
	public void testGetParent_relativeNames()
	{
		DirectoryPath expected = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "", "data");
		DirectoryPath path = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "", expected.toString() + "/tmp");

		DirectoryPath parent = path.getParent();
		assertNotNull(parent);
		assertEquals(expected, parent);
	}

	@Test
	public void testGetParent_relativeNamesDir()
	{
		DirectoryPath expected = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "", "data");
		DirectoryPath path = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "", expected.toString() + "/tmp/");

		DirectoryPath parent = path.getParent();
		assertNotNull(parent);
		assertEquals(expected, parent);
	}

	@Test
	public void testGetParent_ofRoot()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");

		DirectoryPath parent = root.getParent();
		assertNull(parent);
	}

	@Test
	@Ignore
	public void testGetNameCount()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetNameCount_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.getNameCount();
	}

	@Test
	@Ignore
	public void testGetName()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetName_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.getName(0);
	}

	@Test
	@Ignore
	public void testSubpath()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSubpath_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.subpath(0, 1);
	}

	@Test
	@Ignore
	public void testStartsWithPath()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testStartsWithPath_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.startsWith(root);
	}

	@Test
	@Ignore
	public void testStartsWithString()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testStartsWithString_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.startsWith(root.toString());
	}

	@Test
	@Ignore
	public void testEndsWithPath()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEndsWithPath_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.endsWith(root);
	}

	@Test
	@Ignore
	public void testEndsWithString()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEndsWithString_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.endsWith(root.toString());
	}

	@Test
	@Ignore
	public void testNormalize()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNormalize_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.normalize();
	}

	@Test
	public void testResolvePath_otherEmpty()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("");

		Path resolved = path1.resolve(path2);
		assertSame(path1, resolved);
	}

	@Test
	public void testResolvePath_bothAbsolute()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("/test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("/test2");

		Path resolved = path1.resolve(path2);
		assertSame(path2, resolved);
	}

	@Test
	public void testResolvePath_thisRelative()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("/test2");

		Path resolved = path1.resolve(path2);
		assertSame(path2, resolved);
	}

	@Test
	public void testResolvePath_otherRelative()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("/test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("test2");

		Path resolved = path1.resolve(path2);
		assertEquals(path1.toString() + "/" + path2.toString(), resolved.toString());
	}

	@Test
	public void testResolvePath_bothRelative()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("test2");

		Path resolved = path1.resolve(path2);
		assertEquals(path1.toString() + "/" + path2.toString(), resolved.toString());
	}

	@Test
	public void testResolveString_otherEmpty()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("");

		Path resolved = path1.resolve(path2);
		assertSame(path1, resolved);
	}

	@Test
	public void testResolveString_bothAbsolute()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("/test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("/test2");

		Path resolved = path1.resolve(path2.toString());
		assertEquals(path2, resolved);
	}

	@Test
	public void testResolveString_thisRelative()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("/test2");

		Path resolved = path1.resolve(path2.toString());
		assertEquals(path2, resolved);
	}

	@Test
	public void testResolveString_otherRelative()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("/test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("test2");

		Path resolved = path1.resolve(path2.toString());
		assertEquals(path1.toString() + "/" + path2.toString(), resolved.toString());
	}

	@Test
	public void testResolveString_bothRelative()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("test1");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("test2");

		Path resolved = path1.resolve(path2.toString());
		assertEquals(path1.toString() + "/" + path2.toString(), resolved.toString());
	}

	@Test
	@Ignore
	public void testResolveSiblingPath()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testResolveSiblingPath_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.resolveSibling(root);
	}

	@Test
	@Ignore
	public void testResolveSiblingString()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testResolveSiblingString_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.resolveSibling(root.toString());
	}

	@Test
	public void testRelativize_simple()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("/");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("/test");

		Path relativized = path1.relativize(path2);
		assertEquals("test", relativized.toString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRelativize_simpleFail1()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("/");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("/test");

		path2.relativize(path1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRelativize_simpleFail2()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("");
		DirectoryPath path2 = DIRECTORY_FILE_SYSTEM.getPath("/test");

		path2.relativize(path1);
	}

	@Test(expected = ProviderMismatchException.class)
	public void testRelativize_simpleFail3()
	{
		DirectoryPath path1 = DIRECTORY_FILE_SYSTEM.getPath("/");
		Path path2 = Paths.get("/test");

		path2.relativize(path1);
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

	@Test(expected = UnsupportedOperationException.class)
	public void testToUri_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.toUri();
	}

	@Test
	@Ignore
	public void testToAbsolutePath()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testToAbsolutePath_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.toAbsolutePath();
	}

	@Test
	@Ignore
	public void testToRealPath()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testToRealPath_unsupported() throws IOException
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.toRealPath((LinkOption) null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testToFile()
	{
		DirectoryPath directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test");
		directoryPath.toFile();
	}

	@Test
	@Ignore
	public void testRegisterWatchServiceKindOfQArrayModifierArray()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRegisterWatchServiceKindOfQArrayModifierArray_unsupported() throws IOException
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.register(null, null, (WatchEvent.Modifier) null);
	}

	@Test
	@Ignore
	public void testRegisterWatchServiceKindOfQArray()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRegisterWatchServiceKindOfQArray_unsupported() throws IOException
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.register(null, (Kind<?>) null);
	}

	@Test
	@Ignore
	public void testIterator()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIterator_unsupported()
	{
		DirectoryPath root = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/");
		root.iterator();
	}

	@Test
	@Ignore
	public void testCompareTo()
	{
		fail("Not yet implemented");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCompareTo_unsupported()
	{
		DirectoryPath directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test");
		directoryPath.compareTo(directoryPath);
	}

	@Test
	public void testGetFileSystem()
	{
		DirectoryPath directoryPath = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test");
		DirectoryFileSystem fileSystem = directoryPath.getFileSystem();
		assertEquals(DIRECTORY_FILE_SYSTEM, fileSystem);
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

	@Test
	public void testEquals()
	{
		DirectoryPath directoryPath1 = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test");
		DirectoryPath directoryPath2 = new DirectoryPath(DIRECTORY_FILE_SYSTEM, "/", "/test2");

		assertTrue(directoryPath1.equals(directoryPath1));
		assertFalse(directoryPath1.equals(directoryPath2));
		assertFalse(directoryPath1.equals(directoryPath1.toString()));
	}
}
