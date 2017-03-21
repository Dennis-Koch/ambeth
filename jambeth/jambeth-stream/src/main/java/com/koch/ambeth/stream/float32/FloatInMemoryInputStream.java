package com.koch.ambeth.stream.float32;

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
 * Provides a binary stream IEEE 754 bit-encoded float values. These explicitly means that every 4 bytes belong to the same float value
 */
public class FloatInMemoryInputStream implements IFloatInputStream
{
	public static final IFloatInputStream EMPTY_INPUT_STREAM = new IFloatInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public float readFloat()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasFloat()
		{
			return false;
		}
	};

	private int index = -1;

	private final float[] array;

	public FloatInMemoryInputStream(float[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasFloat()
	{
		return (array.length > index + 1);
	}

	@Override
	public float readFloat()
	{
		return array[++index];
	}
}
