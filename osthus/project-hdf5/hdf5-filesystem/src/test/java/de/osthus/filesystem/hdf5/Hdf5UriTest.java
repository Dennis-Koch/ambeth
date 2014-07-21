package de.osthus.filesystem.hdf5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
// TODO Change for HDF5
@Ignore
public class Hdf5UriTest
{
	private static final String SCHEME = Hdf5FileSystemProvider.SCHEME;

	private static final String FILENAME_EXTENSION = Hdf5FileSystemProvider.FILENAME_EXTENSION;

	private static final String TEST_FILENAME = "projectData." + FILENAME_EXTENSION;

	private static final String PATH = "/data/file.txt";

	@Test
	public void test_longWindowsFsUri() throws URISyntaxException
	{
		String str = SCHEME + ":file:///c:/temp/" + TEST_FILENAME;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("file:///c:/temp/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("file:/", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("///c:/temp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertEquals("c:/temp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath2());
		assertEquals("", hdf5Uri.getPath());
	}

	@Test
	public void test_longWindowsPathUri() throws URISyntaxException
	{
		String str = SCHEME + ":file:///c:/temp/" + TEST_FILENAME + "!" + PATH;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("file:///c:/temp/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("file:/", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("///c:/temp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertEquals("c:/temp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath2());
		assertEquals(PATH, hdf5Uri.getPath());
	}

	@Test
	public void test_longWindowsZipFsUri() throws URISyntaxException
	{
		String str = SCHEME + ":jar:file:///c:/temp/arc.zip!/test/" + TEST_FILENAME;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("jar:file:///c:/temp/arc.zip!/test/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("jar:file:///c:/temp/arc.zip", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/test/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertNull(hdf5Uri.getUnderlyingPath2());
		assertEquals("", hdf5Uri.getPath());
	}

	@Test
	public void test_longWindowsZipPathUri() throws URISyntaxException
	{
		String str = SCHEME + ":jar:file:///c:/temp/arc.zip!/test/" + TEST_FILENAME + "!" + PATH;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("jar:file:///c:/temp/arc.zip!/test/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("jar:file:///c:/temp/arc.zip", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/test/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertNull(hdf5Uri.getUnderlyingPath2());
		assertEquals(PATH, hdf5Uri.getPath());
	}

	@Test
	public void test_shortWindowsFsUri() throws URISyntaxException
	{
		String str = SCHEME + ":file:/c:/temp/" + TEST_FILENAME;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("file:/c:/temp/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("file:/", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/c:/temp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertEquals("c:/temp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath2());
		assertEquals("", hdf5Uri.getPath());
	}

	@Test
	public void test_shortWindowsPathUri() throws URISyntaxException
	{
		String str = SCHEME + ":file:/c:/temp/" + TEST_FILENAME + "!" + PATH;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("file:/c:/temp/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("file:/", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/c:/temp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertEquals("c:/temp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath2());
		assertEquals(PATH, hdf5Uri.getPath());
	}

	@Test
	public void test_shortWindowsZipFsUri() throws URISyntaxException
	{
		String str = SCHEME + ":jar:file:/c:/temp/arc.zip!/test/" + TEST_FILENAME;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("jar:file:/c:/temp/arc.zip!/test/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("jar:file:/c:/temp/arc.zip", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/test/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertNull(hdf5Uri.getUnderlyingPath2());
		assertEquals("", hdf5Uri.getPath());
	}

	@Test
	public void test_shortWindowsZipPathUri() throws URISyntaxException
	{
		String str = SCHEME + ":jar:file:/c:/temp/arc.zip!/test/" + TEST_FILENAME + "!" + PATH;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("jar:file:/c:/temp/arc.zip!/test/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("jar:file:/c:/temp/arc.zip", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/test/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertNull(hdf5Uri.getUnderlyingPath2());
		assertEquals(PATH, hdf5Uri.getPath());
	}

	@Test
	public void test_longLinuxFsUri() throws URISyntaxException
	{
		String str = SCHEME + ":file:///tmp/" + TEST_FILENAME;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("file:///tmp/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("file:/", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("///tmp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertEquals("tmp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath2());
		assertEquals("", hdf5Uri.getPath());
	}

	@Test
	public void test_longLinuxPathUri() throws URISyntaxException
	{
		String str = SCHEME + ":file:///tmp/" + TEST_FILENAME + "!" + PATH;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("file:///tmp/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("file:/", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("///tmp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertEquals("tmp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath2());
		assertEquals(PATH, hdf5Uri.getPath());
	}

	@Test
	public void test_longLinuxZipFsUri() throws URISyntaxException
	{
		String str = SCHEME + ":jar:file:///tmp/arc.zip!/test/" + TEST_FILENAME;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("jar:file:///tmp/arc.zip!/test/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("jar:file:///tmp/arc.zip", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/test/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertNull(hdf5Uri.getUnderlyingPath2());
		assertEquals("", hdf5Uri.getPath());
	}

	@Test
	public void test_longLinuxZipPathUri() throws URISyntaxException
	{
		String str = SCHEME + ":jar:file:///tmp/arc.zip!/test/" + TEST_FILENAME + "!" + PATH;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("jar:file:///tmp/arc.zip!/test/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("jar:file:///tmp/arc.zip", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/test/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertNull(hdf5Uri.getUnderlyingPath2());
		assertEquals(PATH, hdf5Uri.getPath());
	}

	@Test
	public void test_shortLinuxFsUri() throws URISyntaxException
	{
		String str = SCHEME + ":file:/tmp/" + TEST_FILENAME;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("file:/tmp/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("file:/", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/tmp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertEquals("tmp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath2());
		assertEquals("", hdf5Uri.getPath());
	}

	@Test
	public void test_shortLinuxPathUri() throws URISyntaxException
	{
		String str = SCHEME + ":file:/tmp/" + TEST_FILENAME + "!" + PATH;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("file:/tmp/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("file:/", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/tmp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertEquals("tmp/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath2());
		assertEquals(PATH, hdf5Uri.getPath());
	}

	@Test
	public void test_shortLinuxZipFsUri() throws URISyntaxException
	{
		String str = SCHEME + ":jar:file:/tmp/arc.zip!/test/" + TEST_FILENAME;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("jar:file:/tmp/arc.zip!/test/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("jar:file:/tmp/arc.zip", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/test/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertNull(hdf5Uri.getUnderlyingPath2());
		assertEquals("", hdf5Uri.getPath());
	}

	@Test
	public void test_shortLinuxZipPathUri() throws URISyntaxException
	{
		String str = SCHEME + ":jar:file:/tmp/arc.zip!/test/" + TEST_FILENAME + "!" + PATH;
		Hdf5Uri hdf5Uri = Hdf5Uri.create(str);

		assertEquals("jar:file:/tmp/arc.zip!/test/" + TEST_FILENAME, hdf5Uri.getIdentifier());
		assertEquals("jar:file:/tmp/arc.zip", hdf5Uri.getUnderlyingFileSystem());
		assertEquals("/test/" + TEST_FILENAME, hdf5Uri.getUnderlyingPath());
		assertNull(hdf5Uri.getUnderlyingPath2());
		assertEquals(PATH, hdf5Uri.getPath());
	}
}
