package com.koch.ambeth.stream.bool;

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
 * Provides a binary stream of byte-encoded boolean values. This explicitly means that 7 bits remain
 * unused.
 */
public class BooleanInMemoryInputStream implements IBooleanInputStream {
	public static final IBooleanInputStream EMPTY_INPUT_STREAM = new IBooleanInputStream() {
		@Override
		public void close() throws IOException {
			// Intended blank
		}

		@Override
		public boolean readBoolean() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasBoolean() {
			return false;
		}
	};

	private int index = -1;

	private final boolean[] array;

	public BooleanInMemoryInputStream(boolean[] array) {
		this.array = array;
	}

	@Override
	public void close() throws IOException {
		// Intended blank
	}

	@Override
	public boolean hasBoolean() {
		return (array.length > index + 1);
	}

	@Override
	public boolean readBoolean() {
		return array[++index];
	}
}
