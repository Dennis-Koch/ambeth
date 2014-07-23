package de.osthus.filesystem.common;

import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystem;

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

	private final S provider;

	@Getter
	private final String identifyer;

	private boolean isOpen = true;

	public AbstractFileSystem(S provider, String identifyer)
	{
		this.provider = provider;
		this.identifyer = identifyer;
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

	protected void checkIsOpen()
	{
		if (!isOpen)
		{
			throw new ClosedFileSystemException();
		}
	}

	protected abstract P buildPath(String rootName, String pathName);
}
