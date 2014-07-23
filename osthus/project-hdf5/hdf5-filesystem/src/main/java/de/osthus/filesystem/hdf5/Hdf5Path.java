package de.osthus.filesystem.hdf5;

import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;

import de.osthus.filesystem.common.AbstractPath;

/**
 * Path implementation for a HDF5-based FileSystem implementation.
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
	public Hdf5Path relativize(Path other)
	{
		if (root.isEmpty() || other.getRoot() == null)
		{
			throw new IllegalArgumentException("Both paths have to be absolute");
		}
		if (!fileSystem.equals(other.getFileSystem()))
		{
			throw new ProviderMismatchException();
		}

		// TODO just a quick and dirty implementation to get things working
		if (other.toString().startsWith(path))
		{
			String relativePathString = other.toString().substring(path.length());
			Hdf5Path relativePath = fileSystem.getPath(relativePathString);
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
}
