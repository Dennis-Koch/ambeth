package de.osthus.filesystem.directory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

import lombok.Getter;

/**
 * Path implementation for a sub-directory-based FileSystem implementation.
 * 
 * @author jochen.hormes
 * @start 2014-07-16
 */
public class DirectoryPath implements Path
{
	/**
	 * {@inheritDoc}
	 */
	@Getter
	private final FileSystem fileSystem;

	private final String rootName;

	private final String pathName;

	/**
	 * {@inheritDoc}
	 */
	@Getter
	private final Path root;

	protected DirectoryPath(FileSystem fileSystem, String rootName, String pathName)
	{
		this.fileSystem = fileSystem;
		this.rootName = rootName;
		this.pathName = pathName;

		if (isAbsolute())
		{
			if (!pathName.isEmpty())
			{
				root = new DirectoryPath(fileSystem, rootName, "");
			}
			else
			{
				root = this;
			}
		}
		else
		{
			root = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAbsolute()
	{
		return !rootName.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path getFileName()
	{
		String fileNameString = pathName.substring(pathName.lastIndexOf(fileSystem.getSeparator()));
		Path fileName = new DirectoryPath(fileSystem, rootName, fileNameString);
		return fileName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path getParent()
	{
		String parentName = pathName + "/..";
		Path parent = new DirectoryPath(fileSystem, rootName, parentName);
		return parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNameCount()
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path getName(int index)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path subpath(int beginIndex, int endIndex)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startsWith(Path other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startsWith(String other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean endsWith(Path other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean endsWith(String other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path normalize()
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path resolve(Path other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path resolve(String other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path resolveSibling(Path other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path resolveSibling(String other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path relativize(Path other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI toUri()
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path toAbsolutePath()
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path toRealPath(LinkOption... options) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File toFile()
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Path> iterator()
	{
		throw new UnsupportedOperationException("Not yet implemented");
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
