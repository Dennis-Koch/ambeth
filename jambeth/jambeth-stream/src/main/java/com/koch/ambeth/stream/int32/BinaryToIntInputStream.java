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
import java.io.InputStream;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BinaryToIntInputStream implements IIntInputStream {
	private final InputStream is;

	private final byte[] input = new byte[4];

	private int inputOffset;

	public BinaryToIntInputStream(InputStream is) {
		this.is = is;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	@Override
	public boolean hasInt() {
		if (inputOffset == 4) {
			return true;
		}
		int length;
		try {
			length = is.read(input, inputOffset, 4 - inputOffset);
		}
		catch (IOException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		inputOffset += length;
		return (inputOffset == 4);
	}

	@Override
	public int readInt() {
		if (inputOffset != 4) {
			throw new IllegalStateException("Not allowed");
		}
		inputOffset = 0;
		byte[] input = this.input;
		// IEEE 754
		int intValue = (input[0] & 0xff) << (3 * 8);
		intValue += (input[1] & 0xff) << (2 * 8);
		intValue += (input[2] & 0xff) << (1 * 8);
		intValue += (input[3] & 0xff) << (0 * 8);
		return intValue;
	}
}
