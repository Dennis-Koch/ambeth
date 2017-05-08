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
import java.io.InputStream;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BinaryToDoubleInputStream implements IDoubleInputStream {
	private final InputStream is;

	private final byte[] input = new byte[8];

	private int inputOffset;

	public BinaryToDoubleInputStream(InputStream is) {
		this.is = is;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	@Override
	public boolean hasDouble() {
		if (inputOffset == 8) {
			return true;
		}
		int length;
		try {
			length = is.read(input, inputOffset, 8 - inputOffset);
		}
		catch (IOException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		inputOffset += length;
		return (inputOffset == 8);
	}

	@Override
	public double readDouble() {
		if (inputOffset != 8) {
			throw new IllegalStateException("Not allowed");
		}
		inputOffset = 0;
		byte[] input = this.input;
		// IEEE 754
		long longValue = ((long) input[0] & 0xff) << (7 * 8);
		longValue += ((long) input[1] & 0xff) << (6 * 8);
		longValue += ((long) input[2] & 0xff) << (5 * 8);
		longValue += ((long) input[3] & 0xff) << (4 * 8);
		longValue += ((long) input[4] & 0xff) << (3 * 8);
		longValue += ((long) input[5] & 0xff) << (2 * 8);
		longValue += ((long) input[6] & 0xff) << (1 * 8);
		longValue += ((long) input[7] & 0xff) << (0 * 8);
		return Double.longBitsToDouble(longValue);
	}
}
