package de.osthus.filesystem.hdf5;

import static ncsa.hdf.hdf5lib.H5.H5Fcreate;
import static ncsa.hdf.hdf5lib.H5.H5Gcreate;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_ACC_TRUNC;
import static ncsa.hdf.hdf5lib.HDF5Constants.H5P_DEFAULT;

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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import de.osthus.filesystem.common.AbstractFileSystemProvider;

/**
 * FileSystemProvider for a HDF5-based FileSystem implementation. It works like the zipFs.
 * 
 * @author jochen.hormes
 * @start 2014-07-23
 */
public class Hdf5FileSystemProvider extends AbstractFileSystemProvider<Hdf5FileSystemProvider, Hdf5FileSystem, Hdf5Uri, Hdf5Path>
{
	protected static final String SCHEME = "hdf5";

	protected static final String FILENAME_EXTENSION = "h5";

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
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException
	{
		if (attrs.length > 0)
		{
			throw new UnsupportedOperationException("Not yet implemented");
		}
		if (!(dir instanceof Hdf5Path))
		{
			throw new IllegalArgumentException("Path not in HDF5 file system: '" + dir + "'");
		}

		Hdf5Path hdf5Dir = (Hdf5Path) dir;
		Hdf5FileSystem fileSystem = hdf5Dir.getFileSystem();
		Path underlyingFile = fileSystem.getUnderlyingFile();

		int file = 0;
		int data = 0;
		try
		{
			file = H5.H5Fopen(underlyingFile.toString(), H5P_DEFAULT, H5P_DEFAULT);
			data = H5Gcreate(file, "data", H5P_DEFAULT, H5P_DEFAULT, H5P_DEFAULT);
		}
		catch (HDF5LibraryException | NullPointerException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (data != 0)
			{
				try
				{
					H5.H5Gclose(data);
				}
				catch (HDF5LibraryException e)
				{
					throw new IOException(e);
				}
			}
			if (file != 0)
			{
				try
				{
					H5.H5Fclose(file);
				}
				catch (HDF5LibraryException e)
				{
					throw new IOException(e);
				}
			}
		}
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

		Hdf5Path hdf5Path = (Hdf5Path) path;
		Hdf5FileSystem fileSystem = hdf5Path.getFileSystem();

		Path underlyingFile = fileSystem.getUnderlyingFile();
		FileSystem underlyingFileSystem = underlyingFile.getFileSystem();
		FileSystemProvider underlyingFileSystemProvider = underlyingFileSystem.provider();

		FileStore fileStore = underlyingFileSystemProvider.getFileStore(underlyingFile);

		return fileStore;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
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
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Auto-generated method stub
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

	// protected Path getRealPath(Path path)
	// {
	// FileSystem fileSystem = path.getFileSystem();
	// if (!(fileSystem instanceof Hdf5FileSystem))
	// {
	// throw new RuntimeException("Path '" + path + "' does not reside in an Hdf5 File System");
	// }
	//
	// Path relativePath = path.getRoot().relativize(path);
	//
	// Hdf5FileSystem directoryFileSystem = (Hdf5FileSystem) fileSystem;
	// Path underlyingFileSystemPath = directoryFileSystem.getUnderlyingFileSystemPath();
	// Path realPath = underlyingFileSystemPath.resolve(relativePath.toString());
	// return realPath;
	// }

	@Override
	protected Hdf5Uri buildInternalUri(URI uri)
	{
		Hdf5Uri internalUri = Hdf5Uri.create(uri);
		return internalUri;
	}

	@Override
	protected Hdf5FileSystem buildFileSystem(Hdf5Uri internalUri, Map<String, ?> env) throws IOException
	{
		URI underlyingFileSystemUri = URI.create(internalUri.getUnderlyingFileSystem());
		FileSystem underlyingFileSystem = findUnderlyingFileSystem(underlyingFileSystemUri, env);
		Path underlyingFilePath = buildUnderlyingFileSystemPath(underlyingFileSystem, internalUri);
		if (!Files.exists(underlyingFilePath))
		{
			createHdf5File(underlyingFilePath);
		}

		String identifier = internalUri.getIdentifier();
		Hdf5FileSystem fileSystem = new Hdf5FileSystem(this, underlyingFilePath, identifier);
		return fileSystem;
	}

	protected void createHdf5File(Path underlyingFilePath) throws IOException
	{
		try
		{
			int file = H5Fcreate(underlyingFilePath.toString(), H5F_ACC_TRUNC, H5P_DEFAULT, H5P_DEFAULT);
			H5.H5Fclose(file);
		}
		catch (HDF5LibraryException | NullPointerException e)
		{
			throw new IOException(e);
		}
	}
}
