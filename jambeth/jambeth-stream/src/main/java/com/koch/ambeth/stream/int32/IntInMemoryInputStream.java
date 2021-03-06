package com.koch.ambeth.stream.int32;

/*-
 * #%L
 * jambeth-stream
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

/**
 * Provides a binary stream IEEE 754 bit-encoded float values. These explicitly means that every 4
 * bytes belong to the same float value
 */
public class IntInMemoryInputStream implements IIntInputStream {
	public static final IIntInputStream EMPTY_INPUT_STREAM = new IIntInputStream() {
		@Override
		public void close() throws IOException {
			// Intended blank
		}

		@Override
		public int readInt() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasInt() {
			return false;
		}
	};

	private int index = -1;

	private final int[] array;

	public IntInMemoryInputStream(int[] array) {
		this.array = array;
	}

	@Override
	public void close() throws IOException {
		// Intended blank
	}

	@Override
	public boolean hasInt() {
		return (array.length > index + 1);
	}

	@Override
	public int readInt() {
		return array[++index];
	}
}
