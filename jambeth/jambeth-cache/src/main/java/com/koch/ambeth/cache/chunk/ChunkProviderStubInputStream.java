package com.koch.ambeth.cache.chunk;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.stream.binary.ReusableByteArrayInputStream;
import com.koch.ambeth.util.collections.ArrayList;

public class ChunkProviderStubInputStream extends InputStream
{
	private static final byte[] EMPTY_BYTES = new byte[0];

	private long position = 0;

	private final Inflater inflater = new Inflater();

	private final ReusableByteArrayInputStream bis = new ReusableByteArrayInputStream(EMPTY_BYTES);

	private InputStream is;

	private boolean deflated, lastChunkReceived;

	protected final IObjRelation self;

	protected final IChunkProvider chunkProvider;

	public ChunkProviderStubInputStream(IObjRelation self, IChunkProvider chunkProvider)
	{
		this.self = self;
		this.chunkProvider = chunkProvider;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int length = deflated ? is.read(b, off, len) : bis.read(b, off, len);
		if (length != -1)
		{
			return length;
		}
		if (lastChunkReceived)
		{
			return -1;
		}
		// length is zero so we have not enough bytes to read at least one byte. So we read the next chunk
		ArrayList<IChunkedRequest> chunkedRequests = new ArrayList<IChunkedRequest>();
		chunkedRequests.add(new ChunkedRequest(self, position, len));
		position += len;
		List<IChunkedResponse> chunkedResponses = chunkProvider.getChunkedContents(chunkedRequests);
		IChunkedResponse chunkedResponse = chunkedResponses.get(0);
		byte[] payload = chunkedResponse.getPayload();
		deflated = chunkedResponse.isDeflated();
		lastChunkReceived = chunkedResponse.getPayloadSize() < len;
		bis.reset(payload);
		if (deflated)
		{
			is = new InflaterInputStream(bis, inflater);
			return is.read(b, off, len);
		}
		is = null;
		return bis.read(b, off, len);
	}

	@Override
	public int read() throws IOException
	{
		throw new UnsupportedOperationException("Should never be called");
	}
}
