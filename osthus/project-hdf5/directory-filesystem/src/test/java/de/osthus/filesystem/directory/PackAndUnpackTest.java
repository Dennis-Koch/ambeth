package de.osthus.filesystem.directory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PackAndUnpackTest
{
	private static final String FOLDER_1_NAME = "src/test/resources/folder1";

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

	private Path unpackDirectory;

	private FileSystem targetFileSystem;

	@Before
	public void setUp() throws Exception
	{
		directoryFileSystemProvider = new DirectoryFileSystemProvider();

		sourceDirectory = Paths.get(FOLDER_1_NAME);

		tempDirectory = Files.createTempDirectory("PackAndUnpackTest_");
		recursiveDeleteOnExit(tempDirectory);

		targetDirectory = Paths.get(tempDirectory.toString(), "target");
		Files.createDirectories(targetDirectory);

		unpackDirectory = Paths.get(tempDirectory.toString(), "unpack");
		Files.createDirectories(unpackDirectory);

		URI uri = new URI("dir:///" + targetDirectory.toUri());
		targetFileSystem = directoryFileSystemProvider.useFileSystem(uri);
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

	private static void recursiveCopy(final Path source, final Path target) throws IOException
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
				Path newPath = target.resolve(relativPath);

				System.out.println("copy " + file + " -> " + newPath);

				Files.copy(file, newPath);
			}
		});
	}

	private static void recursiveCompare(final Path source, final Path target) throws IOException
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
				Path newPath = target.resolve(relativPath);
				Assert.assertTrue("Compare error: '" + file + "' vs. '" + newPath + "'", Files.exists(newPath));
			}
		});
	}

	private static void recursiveDeleteOnExit(Path path) throws IOException
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
