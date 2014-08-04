package de.osthus.filesystem.common;

import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

/**
 * Abstract base class for Osthus FileSystem implementations.
 * 
 * @author jochen.hormes
 * @start 2014-07-23
 */
public abstract class AbstractFileSystem<F extends AbstractFileSystem<F, S, P>, S extends AbstractFileSystemProvider<S, F, ?, P>, P extends AbstractPath<P, F>>
		extends FileSystem
{
	private static final String SEPARATOR = "/";

	protected final S provider;

	@Getter
	protected final String identifier;

	private boolean isOpen = true;

	public AbstractFileSystem(S provider, String identifier)
	{
		this.provider = provider;
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public S provider()
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
		@SuppressWarnings("unchecked")
		F fileSystem = (F) this;
		provider.fileSystemClosed(fileSystem);
		isOpen = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen()
	{
		return isOpen;
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
		Path root = getPath("/");
		List<Path> iterable = Arrays.asList(root);
		return iterable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public P getPath(String first, String... more)
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

		P path = buildPath(rootName, pathName);

		return path;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (!this.getClass().equals(obj.getClass()))
		{
			return false;
		}
		@SuppressWarnings("unchecked")
		F other = (F) obj;
		return equalsInternal(other);
	}

	public boolean equalsInternal(F obj)
	{
		return identifier.equals(obj.identifier) && provider.equals(obj.provider);
	}

	@Override
	public int hashCode()
	{
		return provider.hashCode() + 17 * identifier.hashCode();
	}

	protected void checkIsOpen()
	{
		if (!isOpen)
		{
			throw new ClosedFileSystemException();
		}
	}

	protected abstract P buildPath(String rootName, String pathName);
}
