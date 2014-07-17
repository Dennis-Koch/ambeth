package de.osthus.filesystem.directory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
	protected final DirectoryFileSystem fileSystem;

	protected final String root;

	protected final String path;

	protected DirectoryPath(DirectoryFileSystem fileSystem, String root, String path)
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
	public Path getRoot()
	{
		if (root.isEmpty())
		{
			return null;
		}
		return new DirectoryPath(fileSystem, root, root);
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
	public DirectoryPath getFileName()
	{
		if (path.isEmpty() || path.equals(root))
		{
			return null;
		}

		int lastSeparator = path.lastIndexOf(fileSystem.getSeparator());
		String fileNameString;
		if (lastSeparator == -1) // FIXME Is there a Constant for this meaning of -1
		{
			fileNameString = path;
		}
		fileNameString = path.substring(lastSeparator + 1);
		DirectoryPath fileName = new DirectoryPath(fileSystem, root, fileNameString);

		return fileName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectoryPath getParent()
	{
		if (path.isEmpty() || root.equals(path))
		{
			return null;
		}

		String parentName = path + "/.."; // TODO Shorten path to parent name
		DirectoryPath parent = new DirectoryPath(fileSystem, root, parentName);
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
	public DirectoryPath getName(int index)
	{
		// TODO array for separator indexes
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

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DirectoryPath))
		{
			return false;
		}
		return equals((DirectoryPath) obj);
	}

	public boolean equals(DirectoryPath obj)
	{
		return root.equals(obj.root) && path.equals(obj.path) && fileSystem.equals(obj.fileSystem);
	}
}
