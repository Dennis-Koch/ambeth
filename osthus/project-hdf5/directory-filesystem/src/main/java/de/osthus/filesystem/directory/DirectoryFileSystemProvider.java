package de.osthus.filesystem.directory;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import de.osthus.filesystem.common.AbstractFileSystemProvider;

/**
 * FileSystemProvider for a sub-directory-based FileSystem implementation. It works like the 'subst' command in DOS, but not only on the local file system.
 * 
 * @author jochen.hormes
 * @start 2014-07-15
 */
public class DirectoryFileSystemProvider extends AbstractFileSystemProvider<DirectoryFileSystemProvider, DirectoryFileSystem, DirectoryUri, DirectoryPath>
{
	protected static final String SCHEME = "dir";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getScheme()
	{
		return SCHEME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
	{
		Path realPath = getRealPath(path);
		FileSystemProvider underlyingFileSystemProvider = realPath.getFileSystem().provider();
		FileChannel fileChannel = underlyingFileSystemProvider.newFileChannel(realPath, options, attrs);
		return fileChannel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
	{
		Path realPath = getRealPath(path);
		FileSystemProvider underlyingFileSystemProvider = realPath.getFileSystem().provider();
		SeekableByteChannel byteChannel = underlyingFileSystemProvider.newByteChannel(realPath, options, attrs);
		return byteChannel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException
	{
		Path realDir = getRealPath(dir);
		FileSystemProvider underlyingFileSystemProvider = realDir.getFileSystem().provider();
		DirectoryStream<Path> directoryStream = underlyingFileSystemProvider.newDirectoryStream(realDir, filter);
		return directoryStream;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException
	{
		Path realDir = getRealPath(dir);
		FileSystemProvider underlyingFileSystemProvider = realDir.getFileSystem().provider();
		underlyingFileSystemProvider.createDirectory(realDir, attrs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Path path) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isHidden(Path path) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileStore getFileStore(Path path) throws IOException
	{
		if (!(path instanceof DirectoryPath))
		{
			throw new RuntimeException("Unsupported path type: " + path.getClass());
		}

		DirectoryPath dirPath = (DirectoryPath) path;
		DirectoryFileSystem dirFileSystem = dirPath.getFileSystem();

		FileSystem underlyingFileSystem = dirFileSystem.getUnderlyingFileSystemPath().getFileSystem();
		FileSystemProvider underlyingFileSystemProvider = underlyingFileSystem.provider();
		Path underlyingFileSystemPath = dirFileSystem.getUnderlyingFileSystemPath();

		FileStore fileStore = underlyingFileSystemProvider.getFileStore(underlyingFileSystemPath);

		return fileStore;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException
	{
		Path realPath = getRealPath(path);
		FileSystemProvider underlyingFileSystemProvider = realPath.getFileSystem().provider();
		underlyingFileSystemProvider.checkAccess(realPath, modes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException
	{
		Path realPath = getRealPath(path);
		FileSystemProvider underlyingFileSystemProvider = realPath.getFileSystem().provider();
		A readAttributes = underlyingFileSystemProvider.readAttributes(realPath, type, options);
		return readAttributes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
		// return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub

	}

	protected FileSystem findUnderlyingFileSystem(URI underlyingFileSystemUri, Map<String, ?> env) throws IOException
	{
		FileSystem underlyingFileSystem;
		try
		{
			underlyingFileSystem = FileSystems.newFileSystem(underlyingFileSystemUri, env);
		}
		catch (FileSystemAlreadyExistsException e)
		{
			underlyingFileSystem = FileSystems.getFileSystem(underlyingFileSystemUri);
		}
		return underlyingFileSystem;
	}

	protected Path createUnderlyingFileSystemPath(FileSystem underlyingFileSystem, DirectoryUri directoryUri)
	{
		String underlyingFileSystemPathName = directoryUri.getUnderlyingPath();
		Path underlyingFileSystemPath;
		try
		{
			underlyingFileSystemPath = underlyingFileSystem.getPath(underlyingFileSystemPathName);
		}
		catch (InvalidPathException e)
		{
			underlyingFileSystemPathName = directoryUri.getUnderlyingPath2();
			underlyingFileSystemPath = underlyingFileSystem.getPath(underlyingFileSystemPathName);
		}
		return underlyingFileSystemPath;
	}

	protected DirectoryFileSystem createFileSystem(Path underlyingFileSystemPath, String identifier, Map<String, ?> env) throws IOException
	{
		DirectoryFileSystem directoryFileSystem = new DirectoryFileSystem(this, underlyingFileSystemPath, identifier);
		return directoryFileSystem;
	}

	protected Path getRealPath(Path path)
	{
		FileSystem fileSystem = path.getFileSystem();
		if (!(fileSystem instanceof DirectoryFileSystem))
		{
			throw new RuntimeException("Path '" + path + "' does not reside in a Directory File System");
		}

		Path relativePath = path.getRoot().relativize(path);

		DirectoryFileSystem directoryFileSystem = (DirectoryFileSystem) fileSystem;
		Path underlyingFileSystemPath = directoryFileSystem.getUnderlyingFileSystemPath();
		Path realPath = underlyingFileSystemPath.resolve(relativePath.toString());
		return realPath;
	}

	@Override
	protected DirectoryUri buildInternalUri(URI uri)
	{
		return DirectoryUri.create(uri);
	}

	@Override
	protected DirectoryFileSystem buildFileSystem(DirectoryUri internalUri, Map<String, ?> env) throws IOException
	{
		URI underlyingFileSystemUri = URI.create(internalUri.getUnderlyingFileSystem());
		FileSystem underlyingFileSystem = findUnderlyingFileSystem(underlyingFileSystemUri, env);
		Path underlyingFileSystemPath = createUnderlyingFileSystemPath(underlyingFileSystem, internalUri);
		DirectoryFileSystem fileSystem = createFileSystem(underlyingFileSystemPath, internalUri.getIdentifier(), env);
		return fileSystem;
	}
}
