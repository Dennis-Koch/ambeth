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
import java.io.OutputStream;

public class SplitOutputStream extends OutputStream
{
	private final OutputStream[] os;

	public SplitOutputStream(OutputStream... os)
	{
		this.os = os;
	}

	@Override
	public void write(int b) throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.write(b);
		}
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.write(b);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.flush();
		}
	}

	@Override
	public void close() throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.close();
		}
	}
}
