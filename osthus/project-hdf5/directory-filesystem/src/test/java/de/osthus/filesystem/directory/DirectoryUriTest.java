package de.osthus.filesystem.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URISyntaxException;

import org.junit.Test;

public class DirectoryUriTest
{
	private static final String SCHEME = DirectoryFileSystemProvider.SCHEME;

	@Test(expected = IllegalArgumentException.class)
	public void test_faultyUri() throws URISyntaxException
	{
		DirectoryUri.create(SCHEME + "s:file:///c:/temp/dir/");
	}

	@Test(expected = URISyntaxException.class)
	public void test_faultyUri2() throws URISyntaxException
	{
		new DirectoryUri(SCHEME + ":/file:///c/:temp/dir/");
	}

	@Test
	public void test_longWindowsFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = new DirectoryUri(SCHEME + ":file:///c:/temp/dir/");

		assertEquals("file:///c:/temp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("///c:/temp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("c:/temp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_longWindowsPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":file:///c:/temp/dir/!/data/file.txt");

		assertEquals("file:///c:/temp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("///c:/temp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("c:/temp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_longWindowsZipFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":jar:file:///c:/temp/arc.zip!/test/");

		assertEquals("jar:file:///c:/temp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:///c:/temp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_longWindowsZipPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":jar:file:///c:/temp/arc.zip!/test/!/data/file.txt");

		assertEquals("jar:file:///c:/temp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:///c:/temp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_shortWindowsFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":file:/c:/temp/dir/");

		assertEquals("file:/c:/temp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("/c:/temp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("c:/temp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_shortWindowsPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":file:/c:/temp/dir/!/data/file.txt");

		assertEquals("file:/c:/temp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("/c:/temp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("c:/temp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_shortWindowsZipFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":jar:file:/c:/temp/arc.zip!/test/");

		assertEquals("jar:file:/c:/temp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:/c:/temp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_shortWindowsZipPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":jar:file:/c:/temp/arc.zip!/test/!/data/file.txt");

		assertEquals("jar:file:/c:/temp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:/c:/temp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_longLinuxFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":file:///tmp/dir/");

		assertEquals("file:///tmp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("///tmp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("tmp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_longLinuxPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":file:///tmp/dir/!/data/file.txt");

		assertEquals("file:///tmp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("///tmp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("tmp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_longLinuxZipFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":jar:file:///tmp/arc.zip!/test/");

		assertEquals("jar:file:///tmp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:///tmp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_longLinuxZipPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":jar:file:///tmp/arc.zip!/test/!/data/file.txt");

		assertEquals("jar:file:///tmp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:///tmp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_shortLinuxFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":file:/tmp/dir/");

		assertEquals("file:/tmp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("/tmp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("tmp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_shortLinuxPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":file:/tmp/dir/!/data/file.txt");

		assertEquals("file:/tmp/dir/", directoryUri.getIdentifier());
		assertEquals("file:/", directoryUri.getUnderlyingFileSystem());
		assertEquals("/tmp/dir/", directoryUri.getUnderlyingPath());
		assertEquals("tmp/dir/", directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}

	@Test
	public void test_shortLinuxZipFsUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":jar:file:/tmp/arc.zip!/test/");

		assertEquals("jar:file:/tmp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:/tmp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("", directoryUri.getPath());
	}

	@Test
	public void test_shortLinuxZipPathUri() throws URISyntaxException
	{
		DirectoryUri directoryUri = DirectoryUri.create(SCHEME + ":jar:file:/tmp/arc.zip!/test/!/data/file.txt");

		assertEquals("jar:file:/tmp/arc.zip!/test/", directoryUri.getIdentifier());
		assertEquals("jar:file:/tmp/arc.zip", directoryUri.getUnderlyingFileSystem());
		assertEquals("/test/", directoryUri.getUnderlyingPath());
		assertNull(directoryUri.getUnderlyingPath2());
		assertEquals("/data/file.txt", directoryUri.getPath());
	}
}
