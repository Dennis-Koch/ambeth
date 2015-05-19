package de.osthus.ambeth.stream.binary;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.stream.IInputStream;

public class FileBinaryInputSource implements IBinaryInputSource
{
	protected final File file;

	public FileBinaryInputSource(File file)
	{
		this.file = file;
	}

	@Override
	public IInputStream deriveInputStream()
	{
		return createIInputStream();
	}

	@Override
	public InputStreamToBinaryInputStream deriveBinaryInputStream()
	{
		return createIInputStream();
	}

	private InputStreamToBinaryInputStream createIInputStream()
	{
		try
		{
			return new InputStreamToBinaryInputStream(new BufferedInputStream(new FileInputStream(file)));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
