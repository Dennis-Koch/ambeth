package de.osthus.filesystem.common;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Assert;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-23
 */
public abstract class AbstractPackAndUnpackTest
{
	protected void recursiveCopy(final Path source, final Path target) throws IOException
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

	protected void recursiveCompare(final Path source, final Path target) throws IOException
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

	protected void recursiveDeleteOnExit(Path path) throws IOException
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
