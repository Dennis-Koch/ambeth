package de.osthus.filesystem.common;

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
 * @start 2014-07-21
 */
public abstract class AbstractPath<P extends AbstractPath<P, F>, F extends FileSystem> implements Path
{
	/**
	 * {@inheritDoc}
	 */
	@Getter
	protected final F fileSystem;

	protected final String root;

	protected final String path;

	protected AbstractPath(F fileSystem, String root, String path)
	{
		this.fileSystem = fileSystem;
		this.root = root;
		this.path = path;

		if (!root.isEmpty() && !fileSystem.getSeparator().equals(root))
		{
			throw new RuntimeException("Illegal root: " + root);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public P getRoot()
	{
		if (root.isEmpty())
		{
			return null;
		}
		return create(fileSystem, root, root);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAbsolute()
	{
		return !root.isEmpty() && path.startsWith(root);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public P getFileName()
	{
		if (path.isEmpty() || path.equals(root))
		{
			return null;
		}

		int lastSeparator = path.lastIndexOf(fileSystem.getSeparator());
		String fileNameString;
		if (lastSeparator == -1)
		{
			fileNameString = path;
		}
		fileNameString = path.substring(lastSeparator + 1);
		P fileName = create(fileSystem, root, fileNameString);

		return fileName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public P getParent()
	{
		if (path.isEmpty() || root.equals(path))
		{
			return null;
		}

		int lastIndex = path.lastIndexOf('/');
		if (lastIndex == -1)
		{
			return null;
		}
		if (lastIndex == 0)
		{
			// Root is the parent. Modify lastIndex to leave the root slash.
			lastIndex = 1;
		}
		else if (lastIndex == path.length() - 1)
		{
			lastIndex = path.lastIndexOf('/', lastIndex - 1);
		}
		String parentName = path.substring(0, lastIndex);

		P parent = create(fileSystem, root, parentName);
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
	public P getName(int index)
	{
		// TODO array for separator indexes
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public P subpath(int beginIndex, int endIndex)
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
	public P normalize()
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
		String otherPathString = other.toString();

		if (otherPathString.isEmpty())
		{
			return this;
		}
		if (other.isAbsolute())
		{
			return other;
		}
		else
		{
			Path resolved = fileSystem.getPath(path, otherPathString);
			return resolved;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path resolve(String other)
	{
		Path path = fileSystem.getPath(other);
		return resolve(path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public P resolveSibling(Path other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public P resolveSibling(String other)
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
	public P toAbsolutePath()
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public P toRealPath(LinkOption... options) throws IOException
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
		throw new UnsupportedOperationException();
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

	@Override
	public String toString()
	{
		return path;
	}

	protected abstract P create(F fileSystem, String root, String path);
}
