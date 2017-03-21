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
import java.io.InputStream;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BinaryToBooleanInputStream implements IBooleanInputStream
{
	private final InputStream is;

	private boolean inputValue;

	private boolean hasInput;

	public BinaryToBooleanInputStream(InputStream is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public boolean hasBoolean()
	{
		if (!hasInput)
		{
			int oneByte;
			try
			{
				oneByte = is.read();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			if (oneByte == -1)
			{
				return false;
			}
			inputValue = oneByte != 0;
			hasInput = true;
			return true;
		}
		return hasInput;
	}

	@Override
	public boolean readBoolean()
	{
		if (!hasInput)
		{
			throw new IllegalStateException("Not allowed");
		}
		hasInput = false;
		return inputValue;
	}
}
