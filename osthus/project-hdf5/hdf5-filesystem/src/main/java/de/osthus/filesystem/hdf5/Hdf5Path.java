package de.osthus.filesystem.hdf5;

import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;

import de.osthus.filesystem.common.AbstractPath;

/**
 * Path implementation for a ADF2-based FileSystem implementation.
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
public class Hdf5Path extends AbstractPath<Hdf5Path, Hdf5FileSystem> implements Path
{
	protected Hdf5Path(Hdf5FileSystem fileSystem, String root, String path)
	{
		super(fileSystem, root, path);
	}

	@Override
	protected Hdf5Path create(Hdf5FileSystem fileSystem, String root, String path)
	{
		return new Hdf5Path(fileSystem, root, path);
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
			AbstractPath relativePath = fileSystem.getPath(relativePathString);
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
		if (!(obj instanceof Hdf5Path))
		{
			return false;
		}
		return equals((Hdf5Path) obj);
	}

	public boolean equals(Hdf5Path obj)
	{
		return root.equals(obj.root) && path.equals(obj.path) && fileSystem.equals(obj.fileSystem);
	}
}
