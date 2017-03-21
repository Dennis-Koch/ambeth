package com.koch.ambeth.cache.stream.bytebuffer;

/*-
 * #%L
 * jambeth-cache-stream
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
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
		throw new NonWritableChannelException();
	}
}
