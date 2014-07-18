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
	public DirectoryPath getRoot()
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
		if (lastSeparator == -1)
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
	public DirectoryPath subpath(int beginIndex, int endIndex)
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
	public DirectoryPath normalize()
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
			DirectoryPath resolved = fileSystem.getPath(path, otherPathString);
			return resolved;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path resolve(String other)
	{
		DirectoryPath path = fileSystem.getPath(other);
		return resolve(path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectoryPath resolveSibling(Path other)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectoryPath resolveSibling(String other)
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
		if (root.isEmpty() || other.getRoot() == null)
		{
			throw new IllegalArgumentException("Both paths have to be absolute");
		}

		// TODO just a quick and dirty implementation to get things working
		if (fileSystem.equals(other.getFileSystem()) && other.toString().startsWith(path))
		{
			String relativePathString = other.toString().substring(path.length());
			DirectoryPath relativePath = fileSystem.getPath(relativePathString);
			return relativePath;
		}
		else if (fileSystem.getUnderlyingFileSystem().equals(other.getFileSystem()))
		{
			Path relativePath = fileSystem.getUnderlyingFileSystemPath().relativize(other);
			return relativePath;
		}

		throw new UnsupportedOperationException("Not yet fully implemented");
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
	public DirectoryPath toAbsolutePath()
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectoryPath toRealPath(LinkOption... options) throws IOException
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

	@Override
	public String toString()
	{
		return path;
	}
}
