package com.koch.ambeth.cache.stream.bytebuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * Wraps an ordinary byte array as a {@link SeekableByteChannel}
 */
public class ByteArrayByteChannel implements SeekableByteChannel
{
	private byte[] data;

	private int position = 0;

	private boolean closed;

	public ByteArrayByteChannel(byte[] data)
	{
		this.data = data;
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
		data = null;
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
		while (position < data.length && dst.hasRemaining())
		{
			dst.put(data[position]);
			position++;
		}
		if (position == data.length && writtenBytes == 0)
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
		return data.length;
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException
	{
		if (closed)
		{
			throw new ClosedChannelException();
		}
		throw new NonWritableChannelException();
	}
}