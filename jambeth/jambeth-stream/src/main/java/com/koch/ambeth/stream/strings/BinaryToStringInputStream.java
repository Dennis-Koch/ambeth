package com.koch.ambeth.stream.strings;

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

public class BinaryToStringInputStream implements IStringInputStream {
	private final byte[] buffer = new byte[64];

	private int bufferCurrIndex = -2;

	private int bufferEndIndex = bufferCurrIndex;

	private final InputStream is;

	private final StringBuilder sb = new StringBuilder();

	private String value;

	private boolean hasValue;

	private boolean sbActive;

	public BinaryToStringInputStream(InputStream is) {
		this.is = is;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	@Override
	public boolean hasString() {
		while (!hasValue) {
			if (bufferEndIndex == -1) {
				return false;
			}
			if (bufferCurrIndex == bufferEndIndex) {
				int length;
				try {
					length = is.read(buffer, 0, buffer.length);
				}
				catch (IOException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
				bufferCurrIndex = 0;
				bufferEndIndex = length;
			}
			while (bufferCurrIndex < bufferEndIndex) {
				byte oneByte = buffer[bufferCurrIndex];
				if (!sbActive) {
					if (oneByte == StringToBinaryInputStream.NULL_STRING_BYTE) {
						value = null;
						hasValue = true;
						bufferCurrIndex++;
						break;
					}
					else if (oneByte == StringToBinaryInputStream.VALID_STRING_BYTE) {
						sbActive = true;
						bufferCurrIndex++;
						continue;
					}
					else {
						throw new IllegalStateException("Illegal byte");
					}
				}
				if (oneByte == StringToBinaryInputStream.NULL_STRING_BYTE
						|| oneByte == StringToBinaryInputStream.VALID_STRING_BYTE) {
					value = sb.length() > 0 ? sb.toString() : "";
					hasValue = true;
					sb.setLength(0);
					sbActive = false;
					// Do NOT move the buffer index, because we did not yet consume the current header but
					// instead finished the previously read valid
					// string
					break;
				}
				sb.append((char) oneByte);
				bufferCurrIndex++;
			}
		}
		return hasValue;
	}

	@Override
	public String readString() {
		if (!hasValue) {
			throw new IllegalStateException("Not allowed");
		}
		hasValue = false;
		return value;
	}
}
