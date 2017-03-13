package com.koch.ambeth.cache.stream.bytebuffer;

import java.nio.file.Path;

import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.IPrintable;

public class FileKeyImpl implements FileKey, IImmutableType, IPrintable
{
	private final Path filePath;

	public FileKeyImpl(Path filePath)
	{
		this.filePath = filePath;
	}

	public Path getFilePath()
	{
		return filePath;
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ filePath.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		FileKeyImpl other = (FileKeyImpl) obj;
		return filePath.equals(other.filePath);
	}

	@Override
	public String toString()
	{
		return filePath.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(filePath);
	}
}
