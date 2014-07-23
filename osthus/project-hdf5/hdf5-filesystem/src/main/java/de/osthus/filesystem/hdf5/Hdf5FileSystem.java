package de.osthus.filesystem.hdf5;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;

import lombok.Getter;
import de.osthus.filesystem.common.AbstractFileSystem;

/**
 * HDF5-based File System implementation. It works like the zipfs.
 * 
 * @author jochen.hormes
 * @start 2014-07-23
 */
public class Hdf5FileSystem extends AbstractFileSystem<Hdf5FileSystem, Hdf5FileSystemProvider, Hdf5Path>
{
	@Getter
	private final Path underlyingFile;

	public Hdf5FileSystem(Hdf5FileSystemProvider provider, Path underlyingFile, String identifier)
	{
		super(provider, identifier);
		this.underlyingFile = underlyingFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		if (!isOpen())
		{
			return;
		}
		super.close();
		FileSystem underlyingFileSystem = underlyingFile.getFileSystem();
		if (!FileSystems.getDefault().equals(underlyingFileSystem))
		{
			underlyingFileSystem.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen()
	{
		return super.isOpen() && underlyingFile.getFileSystem().isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly()
	{
		checkIsOpen();
		boolean readOnly = underlyingFile.getFileSystem().isReadOnly();
		return readOnly;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Path> getRootDirectories()
	{
		checkIsOpen();
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<FileStore> getFileStores()
	{
		checkIsOpen();
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> supportedFileAttributeViews()
	{
		checkIsOpen();
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern)
	{
		checkIsOpen();
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService()
	{
		checkIsOpen();
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WatchService newWatchService() throws IOException
	{
		checkIsOpen();
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	@Override
	public String toString()
	{
		checkIsOpen();
		return underlyingFile.toString();
	}

	@Override
	protected Hdf5Path buildPath(String rootName, String pathName)
	{
		Hdf5Path path = new Hdf5Path(this, rootName, pathName);
		return path;
	}
}
