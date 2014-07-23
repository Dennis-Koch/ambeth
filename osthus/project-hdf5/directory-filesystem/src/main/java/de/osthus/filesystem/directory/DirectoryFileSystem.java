package de.osthus.filesystem.directory;

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
 * Sub-directory-based FileSystem implementation. It works like the 'subst' command in DOS, but not only on the local file system.
 * 
 * @author jochen.hormes
 * @start 2014-07-15
 */
public class DirectoryFileSystem extends AbstractFileSystem<DirectoryFileSystem, DirectoryFileSystemProvider, DirectoryPath>
{
	@Getter
	private final FileSystem underlyingFileSystem;

	@Getter
	private final Path underlyingFileSystemPath;

	public DirectoryFileSystem(DirectoryFileSystemProvider provider, FileSystem underlyingFileSystem, Path underlyingFileSystemPath, String identifyer)
	{
		super(provider, identifyer);
		this.underlyingFileSystem = underlyingFileSystem;
		this.underlyingFileSystemPath = underlyingFileSystemPath;
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
		return super.isOpen() && underlyingFileSystem.isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly()
	{
		checkIsOpen();
		return underlyingFileSystem.isReadOnly();
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
		return underlyingFileSystemPath.toString();
	}

	@Override
	protected DirectoryPath buildPath(String rootName, String pathName)
	{
		DirectoryPath path = new DirectoryPath(this, rootName, pathName);
		return path;
	}
}
