package com.koch.ambeth.cache.stream.bytebuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class LargeByteBuffer implements IByteBuffer
{
	private final IFileContentCache fileContentCache;

	private long lastChunk = -1;

	private Reference<ByteBuffer> lastByteBufferR;

	private final long length;

	private final FileKey fileKey;

	private final long virtualChunkSize;

	public LargeByteBuffer(IFileContentCache fileContentCache, FileKey fileKey, long length, long virtualChunkSize)
	{
		this.fileContentCache = fileContentCache;
		this.fileKey = fileKey;
		this.length = length;
		this.virtualChunkSize = virtualChunkSize;
	}

	protected ByteBuffer getLastChunkBuffer()
	{
		return lastByteBufferR != null ? lastByteBufferR.get() : null;
	}

	@Override
	public byte byteAt(long index)
	{
		return byteAtIntern(index, getByteBuffer(index));
	}

	protected ByteBuffer getByteBuffer(long offset)
	{
		long virtualChunkSize = this.virtualChunkSize;
		long chunk = offset / virtualChunkSize;
		if (chunk != lastChunk)
		{
			ByteBuffer bufferToRelease = getLastChunkBuffer();
			lastByteBufferR = null;
			if (bufferToRelease != null)
			{
				fileContentCache.releaseByteBuffer(bufferToRelease);
			}
		}
		// request can be satisfied with the current chunk, if it exists
		ByteBuffer chunkBuffer = getLastChunkBuffer();
		if (chunkBuffer == null)
		{
			long position = chunk * virtualChunkSize;
			long requestedChunkSize = Math.min(virtualChunkSize, length - position);
			ByteBuffer[] content = fileContentCache.getContent(fileKey, position, requestedChunkSize);
			chunkBuffer = content[0];
			lastByteBufferR = new WeakReference<ByteBuffer>(chunkBuffer);
			lastChunk = chunk;
		}
		return chunkBuffer;
	}

	protected byte byteAtIntern(long offset, ByteBuffer chunkBuffer)
	{
		return chunkBuffer.get((int) (offset % virtualChunkSize));
	}

	@Override
	public byte[] getBytes(long offset, int len)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long length()
	{
		return length;
	}

	@Override
	public void writeTo(OutputStream ost, long offset, int length) throws IOException
	{
		ByteBuffer src = getByteBuffer(offset);
		int oldPosition = src.position();
		byte[] buffer = new byte[1024];
		while (length > 0)
		{
			if (!src.hasRemaining())
			{
				src.position(oldPosition);
				src = getByteBuffer(offset);
			}
			int bytesWritten = Math.min(src.remaining(), buffer.length);
			src.get(buffer, 0, bytesWritten);
			ost.write(buffer, 0, bytesWritten);
			length -= bytesWritten;
			offset += bytesWritten;
		}
		src.position(oldPosition);
	}

	@Override
	public void writeTo(ByteBuffer dst, long offset, int length)
	{
		ByteBuffer src = getByteBuffer(offset);
		int oldPosition = src.position();
		while (length > 0)
		{
			if (!src.hasRemaining())
			{
				src.position(oldPosition);
				src = getByteBuffer(offset);
			}
			int currPosition = src.position();
			dst.put(src);
			int bytesWritten = src.position() - currPosition;
			length -= bytesWritten;
			offset += bytesWritten;
		}
		src.position(oldPosition);
	}

	@Override
	public void writeTo(WritableByteChannel dst, long offset, int length) throws IOException
	{
		ByteBuffer src = getByteBuffer(offset);
		int oldPosition = src.position();
		while (length > 0)
		{
			if (!src.hasRemaining())
			{
				src.position(oldPosition);
				src = getByteBuffer(offset);
			}
			int currPosition = src.position();
			dst.write(src);
			int bytesWritten = src.position() - currPosition;
			length -= bytesWritten;
			offset += bytesWritten;
		}
		src.position(oldPosition);
	}
}
