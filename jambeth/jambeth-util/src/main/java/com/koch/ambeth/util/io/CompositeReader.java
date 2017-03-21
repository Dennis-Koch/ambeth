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
import java.io.Reader;
import java.util.Collection;

public class CompositeReader extends Reader
{
	private final Reader[] readers;

	private int readerIndex = 0;

	public CompositeReader(Reader... readers)
	{
		this.readers = readers;
	}

	public CompositeReader(Collection<? extends Reader> readers)
	{
		this.readers = readers.toArray(new Reader[readers.size()]);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		if (readerIndex >= readers.length)
		{
			return -1;
		}
		int bytesRead = readers[readerIndex].read(cbuf, off, len);
		if (bytesRead == -1)
		{
			readerIndex++;
			return 0;
		}
		return bytesRead;
	}

	@Override
	public void close() throws IOException
	{
		for (Reader reader : readers)
		{
			reader.close();
		}
	}
}
