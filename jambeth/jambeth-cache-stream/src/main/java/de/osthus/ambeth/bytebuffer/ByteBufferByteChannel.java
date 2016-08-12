package de.osthus.ambeth.bytebuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * Wraps a {@link IByteBuffer} as a {@link SeekableByteChannel}
 */

public class ByteBufferByteChannel implements SeekableByteChannel
{
	private IByteBuffer byteBuffer;

	private long position = 0;

	private boolean closed;

	public ByteBufferByteChannel(IByteBuffer byteBuffer)
	{
		this.byteBuffer = byteBuffer;
	}

	@Override
	public boolean isOpen()
	{
		return !closed;
	}

	@Override
	public void close() throws IOException
	{
		closed = true;
		byteBuffer = null;
		position = -1;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException
	{
		if (closed)
		{
			throw new ClosedChannelException();
		}
		int writtenBytes = 0;
		long size = size();
		while (position < size && dst.hasRemaining())
		{
			long remaining = size - position;
			int currWrittenBytes = (int) Math.min(remaining, dst.remaining());
			byteBuffer.writeTo(dst, position, currWrittenBytes);
			position += currWrittenBytes;
			writtenBytes += currWrittenBytes;
		}
		if (position == size && writtenBytes == 0)
		{
			return -1;
		}
		return writtenBytes;
	}

	@Override
	public int write(ByteBuffer src) throws IOException
	{
		if (closed)
		{
			throw new ClosedChannelException();
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public long position() throws IOException
	{
		if (closed)
		{
			throw new ClosedChannelException();
		}
		return position;
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException
	{
		if (closed)
		{
			throw new ClosedChannelException();
		}
		position = (int) newPosition;
		return this;
	}

	@Override
	public long size() throws IOException
	{
		if (closed)
		{
			throw new ClosedChannelException();
		}
		return byteBuffer.length();
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException
	{
		if (closed)
		{
			throw new ClosedChannelException();
		}
		throw new UnsupportedOperationException();
	}
}