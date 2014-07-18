package de.osthus.filesystem.directory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FileSystemProvider for a sub-directory-based FileSystem implementation. It works like the 'subst' command in DOS, but not only on the local file system.
 * 
 * @author jochen.hormes
 * @start 2014-07-15
 */
public class DirectoryFileSystemProvider extends FileSystemProvider
{
	private static final String SCHEME = "dir";

	// example dir:file:///C:/temp/target/
	protected static final Pattern FS_URI_PATTERN = Pattern.compile("" + SCHEME + "\\:(([^:]+\\:)[^/]*(//[^/]*)?(/(.+?/)))");
	protected static final int FS_URI_GROUP_IDENTIFIER = 1; // Key for FS: 'file:///C:/temp/target/'
	protected static final int FS_URI_GROUP_SUB_SCHEME = 2; // Scheme of underlying FS: 'file:'
	// Group 3 are the // and the authority (server name), defined as group to be optional
	protected static final int FS_URI_GROUP_SUB_PATH = 4; // Sub path in underlying FS: /C:/temp/target/
	protected static final int FS_URI_GROUP_SUB_PATH_2 = 5; // Sub path for Windows: 'C:/temp/target/'

	// example dir:file:///C:/temp/target/!/insideDirFs/folder
	protected static final Pattern PATH_URI_PATTERN = Pattern.compile("(" + SCHEME + "\\:[^:]+\\:[^/]*(//[^/]*)?/.+?/)(?<=/)!(/.+)");
	protected static final int PATH_URI_GROUP_FS_URI = 1; // URI string for the FS: dir:file:///C:/temp/target/
	// Group 2 are the // and the authority (server name), defined as group to be optional
	protected static final int PATH_URI_GROUP_PATH = 3; // Path inside the FS: /insideDirFs/folder

	private final HashMap<String, DirectoryFileSystem> openFileSystems = new HashMap<>();

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
	public DirectoryFileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException
	{
		Matcher matcher = createFsMatcher(uri);
		String dirFsIdentifier = matcher.group(FS_URI_GROUP_IDENTIFIER);

		if (openFileSystems.containsKey(dirFsIdentifier))
		{
			throw new FileSystemAlreadyExistsException();
		}

		URI underlyingFileSystemUri = createUnderlyingFileSystemUri(matcher);
		FileSystem underlyingFileSystem = findUnderlyingFileSystem(underlyingFileSystemUri, env);
		Path underlyingFileSystemPath = createUnderlyingFileSystemPath(underlyingFileSystem, matcher);

		DirectoryFileSystem directoryFileSystem = createFileSystem(underlyingFileSystem, underlyingFileSystemPath, dirFsIdentifier, env);
		openFileSystems.put(dirFsIdentifier, directoryFileSystem);

		return directoryFileSystem;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirectoryFileSystem getFileSystem(URI uri)
	{
		Matcher matcher = createFsMatcher(uri);
		String dirFsIdentifier = matcher.group(FS_URI_GROUP_IDENTIFIER);

		DirectoryFileSystem directoryFileSystem = openFileSystems.get(dirFsIdentifier);
		if (directoryFileSystem == null)
		{
			throw new FileSystemNotFoundException();
		}

		return directoryFileSystem;
	}

	protected DirectoryFileSystem useFileSystem(URI uri)
	{
		DirectoryFileSystem fileSystem;
		try
		{
			fileSystem = newFileSystem(uri, Collections.<String, Object> emptyMap());
		}
		catch (FileSystemAlreadyExistsException e)
		{
			fileSystem = getFileSystem(uri);
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
	public DirectoryPath getPath(URI uri)
	{
		Matcher matcher = createPathMatcher(uri);
		String fsDirUriString = matcher.group(PATH_URI_GROUP_FS_URI);
		String pathString = matcher.group(PATH_URI_GROUP_PATH);
		if (pathString == null)
		{
			pathString = "";
		}

		URI dirUri;
		try
		{
			dirUri = new URI(fsDirUriString);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		DirectoryFileSystem directoryFileSystem = useFileSystem(dirUri);
		DirectoryPath path = directoryFileSystem.getPath(pathString);

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
		DirectoryStream<Path> newDirectoryStream = underlyingFileSystemProvider.newDirectoryStream(realDir, filter);
		return newDirectoryStream;
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

	protected void fileSystemClosed(DirectoryFileSystem directoryFileSystem)
	{
		String fsIdentifier = directoryFileSystem.getFsIdentifyer();
		openFileSystems.remove(fsIdentifier);
	}

	protected Matcher createFsMatcher(URI uri)
	{
		String path = uri.toString();
		Matcher matcher = FS_URI_PATTERN.matcher(path);
		if (!matcher.matches())
		{
			throw new IllegalArgumentException("Illegal file system URI: " + uri);
		}
		return matcher;
	}

	protected Matcher createPathMatcher(URI uri)
	{
		String path = uri.toString();
		Matcher matcher = PATH_URI_PATTERN.matcher(path);
		if (!matcher.matches())
		{
			throw new IllegalArgumentException("Illegal file system URI: " + uri);
		}
		return matcher;
	}

	protected URI createUnderlyingFileSystemUri(Matcher matcher)
	{
		String underlyingFileSystemScheme = matcher.group(FS_URI_GROUP_SUB_SCHEME) + "/";
		URI underlyingFileSystemUri;
		try
		{
			underlyingFileSystemUri = new URI(underlyingFileSystemScheme);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		return underlyingFileSystemUri;
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

	protected Path createUnderlyingFileSystemPath(FileSystem underlyingFileSystem, Matcher matcher)
	{
		String underlyingFileSystemPathName = matcher.group(FS_URI_GROUP_SUB_PATH);
		Path underlyingFileSystemPath;
		try
		{
			underlyingFileSystemPath = underlyingFileSystem.getPath(underlyingFileSystemPathName);
		}
		catch (InvalidPathException e)
		{
			underlyingFileSystemPathName = matcher.group(FS_URI_GROUP_SUB_PATH_2);
			underlyingFileSystemPath = underlyingFileSystem.getPath(underlyingFileSystemPathName);
		}
		return underlyingFileSystemPath;
	}

	protected DirectoryFileSystem createFileSystem(FileSystem underlyingFileSystem, Path underlyingFileSystemPath, String fsIdentifyer, Map<String, ?> env)
			throws IOException
	{
		DirectoryFileSystem directoryFileSystem = new DirectoryFileSystem(this, underlyingFileSystem, underlyingFileSystemPath, fsIdentifyer);
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
}
