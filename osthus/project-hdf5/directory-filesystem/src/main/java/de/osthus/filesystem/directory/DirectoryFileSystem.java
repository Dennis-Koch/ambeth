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

	@Getter
	private final FileSystem underlyingFileSystem;

	@Getter
	private final Path underlyingFileSystemPath;

	@Getter
	private final String fsIdentifyer;

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
		checkIsOpen();
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
		checkIsOpen();
		return underlyingFileSystem.isReadOnly();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSeparator()
	{
		checkIsOpen();
		return SEPARATOR;
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
	public DirectoryPath getPath(String first, String... more)
	{
		checkIsOpen();

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

	protected void checkIsOpen()
	{
		if (!isOpen)
		{
			throw new ClosedFileSystemException();
		}
	}

}
