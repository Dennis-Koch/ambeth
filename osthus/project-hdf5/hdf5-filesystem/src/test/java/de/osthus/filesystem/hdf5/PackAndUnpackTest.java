package de.osthus.filesystem.hdf5;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.filesystem.common.AbstractPackAndUnpackTest;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
// TODO Change for HDF5
@Ignore
public class PackAndUnpackTest extends AbstractPackAndUnpackTest
{
	private static final String FOLDER_1_NAME = "src/test/resources/folder1";

	private static final String FILE_1_NAME = "src/test/resources/file1.zip";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	private Hdf5FileSystemProvider adf2FileSystemProvider;

	private Path sourceDirectory;

	private Path tempDirectory;

	private Path targetDirectory;

	private Path target2Directory;

	private Path unpackDirectory;

	private FileSystem targetFileSystem;

	private FileSystem target2FileSystem;

	@Before
	public void setUp() throws Exception
	{
		adf2FileSystemProvider = new Hdf5FileSystemProvider();

		sourceDirectory = Paths.get(FOLDER_1_NAME);

		tempDirectory = Files.createTempDirectory("PackAndUnpackTest_");
		recursiveDeleteOnExit(tempDirectory);

		targetDirectory = Paths.get(tempDirectory.toString(), "target");
		Files.createDirectories(targetDirectory);

		target2Directory = Paths.get(tempDirectory.toString(), "target2");
		Files.createDirectories(target2Directory);

		unpackDirectory = Paths.get(tempDirectory.toString(), "unpack");
		Files.createDirectories(unpackDirectory);

		Hdf5Uri uri = Hdf5Uri.create(adf2FileSystemProvider.getScheme() + ":" + targetDirectory.toUri());
		targetFileSystem = adf2FileSystemProvider.useFileSystem(uri);

		Hdf5Uri uri2 = Hdf5Uri.create(adf2FileSystemProvider.getScheme() + ":" + target2Directory.toUri());
		target2FileSystem = adf2FileSystemProvider.useFileSystem(uri2);
	}

	@After
	public void tearDown() throws Exception
	{
		recursiveDeleteOnExit(tempDirectory);
	}

	@Test
	public void testPack() throws IOException
	{
		Path targetDirDirectory = targetFileSystem.getPath("/");
		recursiveCopy(sourceDirectory, targetDirDirectory);
		recursiveCompare(sourceDirectory, targetDirectory);
	}

	@Test
	public void testUnpack() throws IOException
	{
		recursiveCopy(sourceDirectory, targetDirectory);

		Path targetDirDirectory = targetFileSystem.getPath("/");
		recursiveCopy(targetDirDirectory, unpackDirectory);
		recursiveCompare(sourceDirectory, unpackDirectory);
	}

	@Test
	public void testRoundtrip() throws IOException
	{
		Path targetDirDirectory = targetFileSystem.getPath("/");
		recursiveCopy(sourceDirectory, targetDirDirectory);
		recursiveCopy(targetDirDirectory, unpackDirectory);
		recursiveCompare(sourceDirectory, unpackDirectory);
	}

	@Test
	public void testInternalCopy() throws IOException
	{
		recursiveCopy(sourceDirectory, targetDirectory);

		Path targetDirDirectory = targetFileSystem.getPath("/");
		Path target2DirDirectory = target2FileSystem.getPath("/");
		recursiveCopy(targetDirDirectory, target2DirDirectory);
		recursiveCompare(sourceDirectory, target2DirDirectory);
	}

	@Test
	public void testDirToZip() throws IOException
	{
		recursiveCopy(sourceDirectory, targetDirectory);

		Path targetDirDirectory = targetFileSystem.getPath("/");

		URI zipFsUri = prepareEmptyZip();
		FileSystem zipFs = FileSystems.newFileSystem(zipFsUri, Collections.<String, Object> emptyMap());
		Path zipFsDirectory = zipFs.getPath("/");

		recursiveCopy(targetDirDirectory, zipFsDirectory);
		zipFs.close(); // Close and reopen to write to file

		zipFs = FileSystems.newFileSystem(zipFsUri, Collections.<String, Object> emptyMap());
		zipFsDirectory = zipFs.getPath("/");
		recursiveCompare(sourceDirectory, zipFsDirectory);
		zipFs.close();
	}

	@Test
	public void testDirInZip() throws IOException
	{
		URI zipFsUri = prepareEmptyZip();

		FileSystem zipFs = FileSystems.newFileSystem(zipFsUri, Collections.<String, Object> emptyMap());
		Path zipFsTest = zipFs.getPath("/test/");
		zipFs.provider().createDirectory(zipFsTest);
		URI zipRootUri = zipFsTest.toUri();

		String dirFsRoot = "dir:" + zipRootUri.toString() + "!/";
		URI dirFsUri = URI.create(dirFsRoot);
		Hdf5Path dirFsPath = adf2FileSystemProvider.getPath(dirFsUri);

		recursiveCopy(sourceDirectory, dirFsPath);

		// Close and reopen to write to file
		zipFs.close();
		zipFs = FileSystems.newFileSystem(zipFsUri, Collections.<String, Object> emptyMap());

		Path zipFsDirectory = zipFs.getPath("/test/");
		recursiveCompare(sourceDirectory, zipFsDirectory);
		zipFs.close();
	}

	protected URI prepareEmptyZip() throws IOException
	{
		Path file1Path = Paths.get(FILE_1_NAME);
		Path zipPath = tempDirectory.resolve(file1Path.getFileName());
		Files.copy(file1Path, zipPath);
		String zipFsUriString = "jar:" + zipPath.toUri().toString();
		URI zipFsUri = URI.create(zipFsUriString);
		return zipFsUri;
	}
}
