package de.osthus.filesystem.hdf5;

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
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * FileSystemProvider for a HDF5-based FileSystem implementation. It works like the zipFs.
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
public class Hdf5FileSystemProvider extends FileSystemProvider
{
	protected static final String SCHEME = "hdf5";

	protected static final String FILENAME_EXTENSION = "h5";

	private final HashMap<String, Hdf5FileSystem> openFileSystems = new HashMap<>();

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
	public Hdf5FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException
	{
		Hdf5Uri directoryUri = Hdf5Uri.create(uri);
		Hdf5FileSystem directoryFileSystem = newFileSystem(directoryUri, env);
		return directoryFileSystem;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Hdf5FileSystem getFileSystem(URI uri)
	{
		Hdf5Uri directoryUri = Hdf5Uri.create(uri);
		Hdf5FileSystem directoryFileSystem = getFileSystem(directoryUri);
		return directoryFileSystem;
	}

	protected Hdf5FileSystem newFileSystem(Hdf5Uri directoryUri, Map<String, ?> env) throws IOException
	{
		String dirFsIdentifier = directoryUri.getIdentifier();

		if (openFileSystems.containsKey(dirFsIdentifier))
		{
			throw new FileSystemAlreadyExistsException();
		}

		URI underlyingFileSystemUri = URI.create(directoryUri.getUnderlyingFileSystem());
		FileSystem underlyingFileSystem = findUnderlyingFileSystem(underlyingFileSystemUri, env);
		Path underlyingFileSystemPath = createUnderlyingFileSystemPath(underlyingFileSystem, directoryUri);

		Hdf5FileSystem directoryFileSystem = createFileSystem(underlyingFileSystem, underlyingFileSystemPath, dirFsIdentifier, env);
		openFileSystems.put(dirFsIdentifier, directoryFileSystem);

		return directoryFileSystem;
	}

	protected Hdf5FileSystem getFileSystem(Hdf5Uri directoryUri)
	{
		String dirFsIdentifier = directoryUri.getIdentifier();

		Hdf5FileSystem directoryFileSystem = openFileSystems.get(dirFsIdentifier);
		if (directoryFileSystem == null)
		{
			throw new FileSystemNotFoundException();
		}

		return directoryFileSystem;
	}

	protected Hdf5FileSystem useFileSystem(Hdf5Uri directoryUri)
	{
		Hdf5FileSystem fileSystem;
		try
		{
			fileSystem = newFileSystem(directoryUri, Collections.<String, Object> emptyMap());
		}
		catch (FileSystemAlreadyExistsException e)
		{
			fileSystem = getFileSystem(directoryUri);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return fileSystem;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
	{
		// TODO
		return super.newFileChannel(path, options, attrs);
	}

	/**
	 * {@inheritDoc} <br>
	 * e.g. dir:file:///C:/temp/target/!/insideDirFs/folder <br>
	 * e.g. hdf5:file:///C:/temp/target/test.h5!/data/myExperiment
	 */
	@Override
	public Hdf5Path getPath(URI uri)
	{
		Hdf5Uri directoryUri = Hdf5Uri.create(uri);
		Hdf5FileSystem directoryFileSystem = useFileSystem(directoryUri);

		String pathString = directoryUri.getPath();
		Hdf5Path path = directoryFileSystem.getPath(pathString);

		return path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
	{
		Path realPath = getRealPath(path);
		FileSystemProvider underlyingFileSystemProvider = realPath.getFileSystem().provider();
		SeekableByteChannel newByteChannel = underlyingFileSystemProvider.newByteChannel(realPath, options, attrs);
		return newByteChannel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException
	{
		Path realDir = getRealPath(dir);
		FileSystemProvider underlyingFileSystemProvider = realDir.getFileSystem().provider();
		DirectoryStream<Path> newAdf2Stream = underlyingFileSystemProvider.newDirectoryStream(realDir, filter);
		return newAdf2Stream;
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
		if (!(path instanceof Hdf5Path))
		{
			throw new RuntimeException("Unsupported path type: " + path.getClass());
		}

		Hdf5Path dirPath = (Hdf5Path) path;
		Hdf5FileSystem dirFileSystem = dirPath.getFileSystem();

		FileSystem underlyingFileSystem = dirFileSystem.getUnderlyingFileSystem();
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

	@Override
	public String toString()
	{
		return SCHEME + ":///";
	}

	protected void fileSystemClosed(Hdf5FileSystem directoryFileSystem)
	{
		String fsIdentifier = directoryFileSystem.getFsIdentifyer();
		openFileSystems.remove(fsIdentifier);
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

	protected Path createUnderlyingFileSystemPath(FileSystem underlyingFileSystem, Hdf5Uri directoryUri)
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

	protected Hdf5FileSystem createFileSystem(FileSystem underlyingFileSystem, Path underlyingFileSystemPath, String fsIdentifyer, Map<String, ?> env)
			throws IOException
	{
		Hdf5FileSystem directoryFileSystem = new Hdf5FileSystem(this, underlyingFileSystem, underlyingFileSystemPath, fsIdentifyer);
		return directoryFileSystem;
	}

	protected Path getRealPath(Path path)
	{
		FileSystem fileSystem = path.getFileSystem();
		if (!(fileSystem instanceof Hdf5FileSystem))
		{
			throw new RuntimeException("Path '" + path + "' does not reside in an Adf2 File System");
		}

		Path relativePath = path.getRoot().relativize(path);

		Hdf5FileSystem directoryFileSystem = (Hdf5FileSystem) fileSystem;
		Path underlyingFileSystemPath = directoryFileSystem.getUnderlyingFileSystemPath();
		Path realPath = underlyingFileSystemPath.resolve(relativePath.toString());
		return realPath;
	}
}
