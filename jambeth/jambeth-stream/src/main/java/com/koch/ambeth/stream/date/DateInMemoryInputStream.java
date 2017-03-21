package com.koch.ambeth.stream.date;

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
import java.util.Date;

public class DateInMemoryInputStream implements IDateInputStream
{
	public static final IDateInputStream EMPTY_INPUT_STREAM = new IDateInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public Date readDate()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasDate()
		{
			return false;
		}
	};

	private int index = -1;

	private final Date[] array;

	public DateInMemoryInputStream(Date[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasDate()
	{
		return (array.length > index + 1);
	}

	@Override
	public Date readDate()
	{
		return array[++index];
	}
}
