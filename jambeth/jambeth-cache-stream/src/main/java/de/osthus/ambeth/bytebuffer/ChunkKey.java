package de.osthus.ambeth.bytebuffer;

import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class ChunkKey implements IPrintable
{
	private final FileKey fileKey;

	private final long paddedPosition;

	public ChunkKey(FileKey fileKey, long paddedPosition)
	{
		this.fileKey = fileKey;
		this.paddedPosition = paddedPosition;
	}

	public FileKey getFileKey()
	{
		return fileKey;
	}

	public long getPaddedPosition()
	{
		return paddedPosition;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof ChunkKey))
		{
			return false;
		}
		ChunkKey other = (ChunkKey) obj;
		return paddedPosition == other.paddedPosition && fileKey.equals(other.fileKey);
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ (int) (paddedPosition ^ (paddedPosition >> 32)) ^ fileKey.hashCode();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		StringBuilderUtil.appendPrintable(sb, fileKey);
		sb.append('#').append(paddedPosition);
	}
}