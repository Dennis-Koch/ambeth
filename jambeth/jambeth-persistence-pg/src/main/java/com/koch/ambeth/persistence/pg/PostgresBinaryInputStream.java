package com.koch.ambeth.persistence.pg;

/*-
 * #%L
 * jambeth-persistence-pg
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
import java.sql.Blob;
import java.sql.SQLException;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PostgresBinaryInputStream implements IBinaryInputStream, IInputStream
{
	private Blob blob;
	private InputStream is;

	public PostgresBinaryInputStream(Blob blob) throws SQLException
	{
		this.blob = blob;
		is = blob.getBinaryStream();
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			blob.free();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public int readByte()
	{
		try
		{
			return is.read();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
