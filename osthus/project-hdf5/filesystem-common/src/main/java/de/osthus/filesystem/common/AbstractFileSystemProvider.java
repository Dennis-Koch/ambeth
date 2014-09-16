package de.osthus.filesystem.common;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for Osthus FileSystemProvider implementations.
 * 
 * @author jochen.hormes
 * @start 2014-07-23
 */
public abstract class AbstractFileSystemProvider<S extends AbstractFileSystemProvider<S, F, U, P>, F extends AbstractFileSystem<F, S, P>, U extends AbstractUri, P extends AbstractPath<P, F>>
		extends FileSystemProvider
{
	private final HashMap<String, F> openFileSystems = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public F newFileSystem(URI uri, Map<String, ?> env) throws IOException
	{
		U internalUri = buildInternalUri(uri);
		F fileSystem = newFileSystem(internalUri, env);
		return fileSystem;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public F getFileSystem(URI uri)
	{
		U internalUri = buildInternalUri(uri);
		F fileSystem = getFileSystem(internalUri);
		return fileSystem;
	}

	protected F newFileSystem(U internalUri, Map<String, ?> env) throws IOException
	{
		String identifier = internalUri.getIdentifier();

		if (openFileSystems.containsKey(identifier))
		{
			throw new FileSystemAlreadyExistsException();
		}

		F fileSystem = buildFileSystem(internalUri, env);
		openFileSystems.put(identifier, fileSystem);

		return fileSystem;
	}

	protected F getFileSystem(U internalUri)
	{
		String identifier = internalUri.getIdentifier();

		F fileSystem = openFileSystems.get(identifier);
		if (fileSystem == null)
		{
			throw new FileSystemNotFoundException();
		}

		return fileSystem;
	}

	public F useFileSystem(U internalUri)
	{
		F fileSystem;
		try
		{
			fileSystem = newFileSystem(internalUri, Collections.<String, Object> emptyMap());
		}
		catch (FileSystemAlreadyExistsException e)
		{
			fileSystem = getFileSystem(internalUri);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return fileSystem;
	}

	/**
	 * {@inheritDoc} <br>
	 * e.g. dir:file:///C:/temp/target/!/insideDirFs/folder <br>
	 * e.g. hdf5:file:///C:/temp/target/test.h5!/data/myExperiment
	 */
	@Override
	public P getPath(URI uri)
	{
		U internalUri = buildInternalUri(uri);
		F fileSystem = useFileSystem(internalUri);

		String pathString = internalUri.getPath();
		P path = fileSystem.getPath(pathString);

		return path;
	}

	@Override
	public String toString()
	{
		return getScheme() + ":///";
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
		S other = (S) obj;
		return equalsInternal(other);
	}

	public boolean equalsInternal(S obj)
	{
		return getScheme().equals(obj.getScheme());
	}

	@Override
	public int hashCode()
	{
		return 17 * getScheme().hashCode();
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

	protected Path buildUnderlyingFileSystemPath(FileSystem underlyingFileSystem, U internalUri)
	{
		String underlyingFileSystemPathName = internalUri.getUnderlyingPath();
		Path underlyingFileSystemPath;
		try
		{
			underlyingFileSystemPath = underlyingFileSystem.getPath(underlyingFileSystemPathName);
		}
		catch (InvalidPathException e)
		{
			underlyingFileSystemPathName = internalUri.getUnderlyingPath2();
			underlyingFileSystemPath = underlyingFileSystem.getPath(underlyingFileSystemPathName);
		}
		return underlyingFileSystemPath;
	}

	protected void fileSystemClosed(F fileSystem)
	{
		String identifier = fileSystem.getIdentifier();
		openFileSystems.remove(identifier);
	}

	protected abstract U buildInternalUri(URI uri);

	protected abstract F buildFileSystem(U internalUri, Map<String, ?> env) throws IOException;
}