package de.osthus.filesystem.directory;

import java.io.IOException;
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

/**
 * Sub-directory-based FileSystem implementation. It works like the 'subst' command in DOS, but not only on the local file system.
 * 
 * @author jochen.hormes
 * @start 2014-07-15
 */
public class DirectoryFileSystem extends FileSystem
{
	private static final String SEPARATOR = "/";

	protected final DirectoryFileSystemProvider provider;

	protected final FileSystem underlyingFileSystem;

	protected final Path underlyingFileSystemPath;

	protected final String fsIdentifyer;

	private boolean isOpen = true;

	public DirectoryFileSystem(DirectoryFileSystemProvider provider, FileSystem underlyingFileSystem, Path underlyingFileSystemPath, String fsIdentifyer)
	{
		this.provider = provider;
		this.underlyingFileSystem = underlyingFileSystem;
		this.underlyingFileSystemPath = underlyingFileSystemPath;
		this.fsIdentifyer = fsIdentifyer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileSystemProvider provider()
	{
		if (!isOpen)
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
		if (!isOpen)
		{
			return;
		}
		if (!FileSystems.getDefault().equals(underlyingFileSystem))
		{
			underlyingFileSystem.close();
		}
		provider.fileSystemClosed(this);
		isOpen = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen()
	{
		return isOpen && underlyingFileSystem.isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly()
	{
		if (!isOpen)
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
		if (!isOpen)
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
		if (!isOpen)
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
		if (!isOpen)
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
		if (!isOpen)
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
	public DirectoryPath getPath(String first, String... more)
	{
		if (!isOpen)
		{
			throw new ClosedFileSystemException();
		}

		String pathName = first;
		String separator = SEPARATOR;
		if (more.length > 0)
		{
			StringBuilder sb = new StringBuilder(pathName);
			for (String next : more)
			{
				if (sb.length() > 0)
				{
					sb.append(separator);
				}
				sb.append(next);
			}
			pathName = sb.toString();
		}

		pathName = pathName.replaceAll("\\\\", "/");
		pathName = pathName.replaceAll("//+", "/");

		String rootName;
		if (pathName.startsWith(separator))
		{
			rootName = separator;
		}
		else
		{
			rootName = "";
		}

		DirectoryPath path = new DirectoryPath(this, rootName, pathName);

		return path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern)
	{
		if (!isOpen)
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
		if (!isOpen)
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
		if (!isOpen)
		{
			throw new ClosedFileSystemException();
		}
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	@Override
	public String toString()
	{
		return provider.toString() + fsIdentifyer;
	}

}
