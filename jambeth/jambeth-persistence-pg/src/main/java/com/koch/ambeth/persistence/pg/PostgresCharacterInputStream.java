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
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.chars.ICharacterInputStream;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PostgresCharacterInputStream implements ICharacterInputStream, IInputStream
{
	private Clob clob;

	private Reader reader;

	public PostgresCharacterInputStream(Clob clob) throws SQLException
	{
		this.clob = clob;
		reader = clob.getCharacterStream();
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			clob.free();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public int readChar()
	{
		try
		{
			return reader.read();
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
