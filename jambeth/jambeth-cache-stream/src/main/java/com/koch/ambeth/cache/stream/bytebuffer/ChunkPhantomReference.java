package com.koch.ambeth.cache.stream.bytebuffer;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;

public class ChunkPhantomReference extends PhantomReference<ByteBuffer>
{
	private final ChunkKey chunkKey;

	public ChunkPhantomReference(ByteBuffer referent, ReferenceQueue<? super ByteBuffer> q, ChunkKey chunkKey)
	{
		super(referent, q);
		this.chunkKey = chunkKey;
	}

	public ChunkKey getChunkKey()
	{
		return chunkKey;
	}
}
