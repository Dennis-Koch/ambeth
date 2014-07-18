package de.osthus.filesystem.directory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PackAndUnpackTest
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

	private DirectoryFileSystemProvider directoryFileSystemProvider;

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
		directoryFileSystemProvider = new DirectoryFileSystemProvider();

		sourceDirectory = Paths.get(FOLDER_1_NAME);

		tempDirectory = Files.createTempDirectory("PackAndUnpackTest_");
		recursiveDeleteOnExit(tempDirectory);

		targetDirectory = Paths.get(tempDirectory.toString(), "target");
		Files.createDirectories(targetDirectory);

		target2Directory = Paths.get(tempDirectory.toString(), "target2");
		Files.createDirectories(target2Directory);

		unpackDirectory = Paths.get(tempDirectory.toString(), "unpack");
		Files.createDirectories(unpackDirectory);

		URI uri = URI.create("dir:" + targetDirectory.toUri());
		targetFileSystem = directoryFileSystemProvider.useFileSystem(uri);

		URI uri2 = URI.create("dir:" + target2Directory.toUri());
		target2FileSystem = directoryFileSystemProvider.useFileSystem(uri2);
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

	// TODO The usage of folders inside a zip file is currently not supported due to problems with parsing the URI
	// @Test
	// public void testDirInZip() throws IOException
	// {
	// URI zipFsUri = copyEmptyZip();
	//
	// FileSystem zipFs = FileSystems.newFileSystem(zipFsUri, Collections.<String, Object> emptyMap());
	// Path zipFsTest = zipFs.getPath("/test");
	// zipFs.provider().createDirectory(zipFsTest);
	// URI zipRootUri = zipFsTest.toUri();
	// System.out.println(zipRootUri.toString());
	//
	// String dirFsRoot = "dir:" + zipRootUri.toString();
	// System.out.println(dirFsRoot.toString());
	//
	// // TODO
	// }

	protected URI prepareEmptyZip() throws IOException
	{
		Path file1Path = Paths.get(FILE_1_NAME);
		Path zipPath = tempDirectory.resolve(file1Path.getFileName());
		Files.copy(file1Path, zipPath);
		String zipFsUriString = "jar:" + zipPath.toUri().toString();
		URI zipFsUri = URI.create(zipFsUriString);
		return zipFsUri;
	}

	private void recursiveCopy(final Path source, final Path target) throws IOException
	{
		Files.walkFileTree(source, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				copyItem(source, target, file);

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
			{
				if (dir.equals(source))
				{
					return FileVisitResult.CONTINUE;
				}

				copyItem(source, target, dir);

				return FileVisitResult.CONTINUE;
			}

			private void copyItem(final Path source, final Path target, Path file) throws IOException
			{
				Path relativPath = source.relativize(file);
				String relativePathString = relativPath.toString();
				Path newPath = target.resolve(relativePathString);
				Files.copy(file, newPath);
			}
		});
	}

	private void recursiveCompare(final Path source, final Path target) throws IOException
	{
		Files.walkFileTree(source, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				compareItem(source, target, file);

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
			{
				if (dir.equals(source))
				{
					return FileVisitResult.CONTINUE;
				}

				compareItem(source, target, dir);

				return FileVisitResult.CONTINUE;
			}

			private void compareItem(final Path source, final Path target, Path file) throws IOException
			{
				Path relativPath = source.relativize(file);
				String relativePathString = relativPath.toString();
				Path newPath = target.resolve(relativePathString);
				Assert.assertTrue("Compare error: '" + file + "' vs. '" + newPath + "'", Files.exists(newPath));
			}
		});
	}

	private void recursiveDeleteOnExit(Path path) throws IOException
	{
		Files.walkFileTree(path, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			{
				file.toFile().deleteOnExit();
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			{
				dir.toFile().deleteOnExit();
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
