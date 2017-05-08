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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;

public class ChunkPhantomReference extends PhantomReference<ByteBuffer> {
	private final ChunkKey chunkKey;

	public ChunkPhantomReference(ByteBuffer referent, ReferenceQueue<? super ByteBuffer> q,
			ChunkKey chunkKey) {
		super(referent, q);
		this.chunkKey = chunkKey;
	}

	public ChunkKey getChunkKey() {
		return chunkKey;
	}
}
