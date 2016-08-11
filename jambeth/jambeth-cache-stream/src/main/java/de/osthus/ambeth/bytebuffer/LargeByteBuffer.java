package de.osthus.ambeth.bytebuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

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
		long virtualChunkSize = this.virtualChunkSize;
		long chunk = index / virtualChunkSize;
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
		return byteAtIntern(index, chunkBuffer);
	}

	protected byte byteAtIntern(long index, ByteBuffer chunkBuffer)
	{
		return chunkBuffer.get((int) (index % virtualChunkSize));
	}

	@Override
	public byte[] getBytes(int offset, int len)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long length()
	{
		return length;
	}

	@Override
	public void writeToOutputStream(OutputStream ost, long offset, long length) throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
