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

import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public class ChunkKey implements IPrintable {
	private final FileKey fileKey;

	private final long paddedPosition;

	public ChunkKey(FileKey fileKey, long paddedPosition) {
		this.fileKey = fileKey;
		this.paddedPosition = paddedPosition;
	}

	public FileKey getFileKey() {
		return fileKey;
	}

	public long getPaddedPosition() {
		return paddedPosition;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ChunkKey)) {
			return false;
		}
		ChunkKey other = (ChunkKey) obj;
		return paddedPosition == other.paddedPosition && fileKey.equals(other.fileKey);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ (int) (paddedPosition ^ (paddedPosition >> 32))
				^ fileKey.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		StringBuilderUtil.appendPrintable(sb, fileKey);
		sb.append('#').append(paddedPosition);
	}
}
