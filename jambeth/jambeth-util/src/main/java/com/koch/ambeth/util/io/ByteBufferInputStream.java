package com.koch.ambeth.util.io;

/*-
 * #%L
 * jambeth-util
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
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
	protected final ByteBuffer[] byteBuffers;

	protected ByteBuffer currentByteBuffer;

	protected int currentIndex = -1;

	protected long available;

	public ByteBufferInputStream(ByteBuffer... byteBuffers) {
		this.byteBuffers = byteBuffers;
		incCurrentIndex();
	}

	protected void incCurrentIndex() {
		if (byteBuffers.length <= currentIndex + 1) {
			currentByteBuffer = null;
			return;
		}
		currentIndex++;
		currentByteBuffer = byteBuffers[currentIndex];
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int available() throws IOException {
		return (int) Math.min(Integer.MAX_VALUE, available);
	}

	@Override
	public int read() throws IOException {
		if (!moveToNextAvailableByte()) {
			return -1;
		}
		int result = currentByteBuffer.get();
		available--;
		return result;
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	protected boolean moveToNextAvailableByte() {
		while (currentByteBuffer != null && !currentByteBuffer.hasRemaining()) {
			incCurrentIndex();
		}
		return currentByteBuffer != null && currentByteBuffer.hasRemaining();
	}

	@Override
	public long skip(long n) throws IOException {
		long skippedBytes = 0;
		do {
			if (!moveToNextAvailableByte()) {
				break;
			}
			int currentSkip = (int) Math.min(currentByteBuffer.remaining(), n);
			currentByteBuffer.position(currentByteBuffer.position() + currentSkip);
			n -= currentSkip;
			skippedBytes += currentSkip;
			available -= currentSkip;
		}
		while (n > 0);
		return skippedBytes;
	}
}
