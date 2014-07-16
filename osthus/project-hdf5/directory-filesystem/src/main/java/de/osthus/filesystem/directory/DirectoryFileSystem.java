package de.osthus.filesystem.directory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Sub-directory-based FileSystem implementation. It works like the 'subst' command in DOS, but not only on the local file system.
 * 
 * @author jochen.hormes
 * @start 2014-07-15
 */
public class DirectoryFileSystem extends FileSystem
{

	private static final String SEPARATOR = "/";

	private final DirectoryFileSystemProvider provider;

	private final FileSystem underlyingFileSystem;

	@Getter(AccessLevel.PROTECTED)
	private final URI underlyingFileSystemUri;

	private final Path underlyingFileSystemPath;

	private boolean closed = false;

	public DirectoryFileSystem(FileSystem underlyingFileSystem, URI underlyingFileSystemUri, Path underlyingFileSystemPath, DirectoryFileSystemProvider provider)
	{
		this.underlyingFileSystem = underlyingFileSystem;
		this.underlyingFileSystemUri = underlyingFileSystemUri;
		this.underlyingFileSystemPath = underlyingFileSystemPath;
		this.provider = provider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileSystemProvider provider()
	{
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
		return provider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		if (closed)
		{
			return;
		}
		if (!FileSystems.getDefault().equals(underlyingFileSystem))
		{
			underlyingFileSystem.close();
		}
		provider.fileSystemClosed(this);
		closed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen()
	{
		return !closed && underlyingFileSystem.isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly()
	{
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
		return underlyingFileSystem.isReadOnly();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSeparator()
	{
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
		return SEPARATOR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Path> getRootDirectories()
	{
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
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
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
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
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path getPath(String first, String... more)
	{
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
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
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
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
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
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
		if (closed)
		{
			throw new ClosedFileSystemException();
		}
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

}
