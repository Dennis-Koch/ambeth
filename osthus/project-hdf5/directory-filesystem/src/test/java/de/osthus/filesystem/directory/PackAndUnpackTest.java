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
		recursiveCopyDirFS(sourceDirectory, targetDirDirectory);
		// TODO copy all file and folders from the sourceDirectory to the targetDirectory via the DirectoryFileSystem.
		// TODO check result
	}

	@Test
	public void testUnpack() throws IOException
	{
		recursiveCopyJava(sourceDirectory, targetDirectory);
		// TODO copy all file and folders from the targetDirectory to the unpackDirectory via the DirectoryFileSystem.
		// TODO check result
	}

	@Test
	public void testRoundtrip()
	{
		// TODO copy all file and folders from the sourceDirectory to the targetDirectory and to the unpackDirectory via the DirectoryFileSystem.
		// TODO check result
	}

	private static void recursiveCopyDirFS(final Path source, final Path target) throws IOException
	{
		Files.walkFileTree(source, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				System.out.println("file: " + file + " -> " + target);

				Path relativPath = source.relativize(file);
				Path newPath = target.resolve(relativPath);
				Files.copy(file, newPath);

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
			{
				System.out.println("dir: " + dir + " -> " + target);

				Path relativPath = source.relativize(dir);
				Path newPath = target.resolve(relativPath);
				Files.createDirectories(newPath);

				// dir.toFile().deleteOnExit();
				//
				// FileSystem targetFileSystem = target.getFileSystem();
				// FileSystemProvider targetFileSystemProvider = targetFileSystem.provider();
				// try
				// {
				// targetFileSystemProvider.createDirectory(dir);
				// }
				// catch (IOException e)
				// {
				// throw new RuntimeException(e);
				// }

				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void recursiveCopyJava(final Path source, final Path target) throws IOException
	{
		Files.walkFileTree(source, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				Path relativPath = source.relativize(file);
				Path newPath = target.resolve(relativPath);
				Files.copy(file, newPath);

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
			{
				Path relativPath = source.relativize(dir);
				Path newPath = target.resolve(relativPath);
				Files.createDirectories(newPath);

				return FileVisitResult.CONTINUE;
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
