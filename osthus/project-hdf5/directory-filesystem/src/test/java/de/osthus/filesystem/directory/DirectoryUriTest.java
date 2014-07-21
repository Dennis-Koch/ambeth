package de.osthus.filesystem.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URISyntaxException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DirectoryUriTest
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
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
	public void test_longWindowsFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":file:///c:/temp/dir/");

		assertEquals("file:///c:/temp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("///c:/temp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("c:/temp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_longWindowsPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":file:///c:/temp/dir/!/data/file.txt");

		assertEquals("file:///c:/temp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("///c:/temp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("c:/temp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_longWindowsZipFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":jar:file:///c:/temp/arc.zip!/test/");

		assertEquals("jar:file:///c:/temp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:///c:/temp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_longWindowsZipPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":jar:file:///c:/temp/arc.zip!/test/!/data/file.txt");

		assertEquals("jar:file:///c:/temp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:///c:/temp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_shortWindowsFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":file:/c:/temp/dir/");

		assertEquals("file:/c:/temp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("/c:/temp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("c:/temp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_shortWindowsPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":file:/c:/temp/dir/!/data/file.txt");

		assertEquals("file:/c:/temp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("/c:/temp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("c:/temp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_shortWindowsZipFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":jar:file:/c:/temp/arc.zip!/test/");

		assertEquals("jar:file:/c:/temp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:/c:/temp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_shortWindowsZipPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":jar:file:/c:/temp/arc.zip!/test/!/data/file.txt");

		assertEquals("jar:file:/c:/temp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:/c:/temp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_longLinuxFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":file:///tmp/dir/");

		assertEquals("file:///tmp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("///tmp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("tmp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_longLinuxPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":file:///tmp/dir/!/data/file.txt");

		assertEquals("file:///tmp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("///tmp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("tmp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_longLinuxZipFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":jar:file:///tmp/arc.zip!/test/");

		assertEquals("jar:file:///tmp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:///tmp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_longLinuxZipPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":jar:file:///tmp/arc.zip!/test/!/data/file.txt");

		assertEquals("jar:file:///tmp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:///tmp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_shortLinuxFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":file:/tmp/dir/");

		assertEquals("file:/tmp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("/tmp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("tmp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_shortLinuxPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":file:/tmp/dir/!/data/file.txt");

		assertEquals("file:/tmp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("/tmp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("tmp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_shortLinuxZipFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":jar:file:/tmp/arc.zip!/test/");

		assertEquals("jar:file:/tmp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:/tmp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_shortLinuxZipPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(DirectoryFileSystemProvider.SCHEME + ":jar:file:/tmp/arc.zip!/test/!/data/file.txt");

		assertEquals("jar:file:/tmp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:/tmp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}
}
