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
