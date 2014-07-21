package de.osthus.filesystem.directory;

import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;

import de.osthus.filesystem.common.AbstractPath;

/**
 * Path implementation for a sub-directory-based FileSystem implementation.
 * 
 * @author jochen.hormes
 * @start 2014-07-16
 */
public class DirectoryPath extends AbstractPath<DirectoryPath, DirectoryFileSystem> implements Path
{
	protected DirectoryPath(DirectoryFileSystem fileSystem, String root, String path)
	{
		super(fileSystem, root, path);
	}

	@Override
	protected DirectoryPath create(DirectoryFileSystem fileSystem, String root, String path)
	{
		return new DirectoryPath(fileSystem, root, path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path relativize(Path other)
	{
		if (root.isEmpty() || other.getRoot() == null)
		{
			throw new IllegalArgumentException("Both paths have to be absolute");
		}
		boolean isSameFileSystem = fileSystem.equals(other.getFileSystem());
		boolean isUnderlyingFileSystem = fileSystem.getUnderlyingFileSystem().equals(other.getFileSystem());
		if (!isSameFileSystem && !isUnderlyingFileSystem)
		{
			throw new ProviderMismatchException();
		}

		// TODO just a quick and dirty implementation to get things working
		if (isSameFileSystem && other.toString().startsWith(path))
		{
			String relativePathString = other.toString().substring(path.length());
			DirectoryPath relativePath = fileSystem.getPath(relativePathString);
			return relativePath;
		}
		else if (isUnderlyingFileSystem)
		{
			Path relativePath = fileSystem.getUnderlyingFileSystemPath().relativize(other);
			return relativePath;
		}

		throw new UnsupportedOperationException("Not yet fully implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Path other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return 0;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DirectoryPath))
		{
			return false;
		}
		return equals((DirectoryPath) obj);
	}

	public boolean equals(DirectoryPath obj)
	{
		return root.equals(obj.root) && path.equals(obj.path) && fileSystem.equals(obj.fileSystem);
	}
}
