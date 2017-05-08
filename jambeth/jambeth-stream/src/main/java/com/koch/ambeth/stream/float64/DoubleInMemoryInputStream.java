package com.koch.ambeth.stream.float64;

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
 * Provides a binary stream of IEEE 754 bit-encoded double values. These explicitly means that every
 * 8 bytes belong to the same double value
 */
public class DoubleInMemoryInputStream implements IDoubleInputStream {
	public static final IDoubleInputStream EMPTY_INPUT_STREAM = new IDoubleInputStream() {
		@Override
		public void close() throws IOException {
			// Intended blank
		}

		@Override
		public double readDouble() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasDouble() {
			return false;
		}
	};

	private int index = -1;

	private final double[] array;

	public DoubleInMemoryInputStream(double[] array) {
		this.array = array;
	}

	@Override
	public void close() throws IOException {
		// Intended blank
	}

	@Override
	public boolean hasDouble() {
		return (array.length > index + 1);
	}

	@Override
	public double readDouble() {
		return array[++index];
	}
}
