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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PostgresClob implements Clob
{
	private long oid;

	private LargeObjectManager largeObjectManager;

	public PostgresClob(PGConnection connection, long oid) throws SQLException
	{
		this.oid = oid;
		largeObjectManager = connection.getLargeObjectAPI();
	}

	@Override
	public void free() throws SQLException
	{
		largeObjectManager = null;
	}

	@Override
	public long length() throws SQLException
	{
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.READ);
		try
		{
			return lo.size64();
		}
		finally
		{
			lo.close();
		}
	}

	@Override
	public InputStream getAsciiStream() throws SQLException
	{
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.READ);
		return lo.getInputStream();
	}

	@Override
	public long position(Clob searchstr, long start) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long position(String searchstr, long start) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getCharacterStream() throws SQLException
	{
		InputStream is = getAsciiStream();
		return new InputStreamReader(is);
	}

	@Override
	public Reader getCharacterStream(long pos, final long length) throws SQLException
	{
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.READ);
		lo.seek64(pos, LargeObject.SEEK_SET);

		final InputStream is = lo.getInputStream();

		return new InputStreamReader(new InputStream()
		{
			long remaining = length;

			@Override
			public int read() throws IOException
			{
				if (remaining > 0)
				{
					remaining--;
				}
				else
				{
					return -1;
				}
				return is.read();
			}

			@Override
			public void close() throws IOException
			{
				is.close();
			}
		});
	}

	@Override
	public String getSubString(long pos, int length) throws SQLException
	{
		Reader reader = getCharacterStream(pos, length);
		try
		{
			StringBuilder sb = new StringBuilder(length);
			int oneByte;
			while ((oneByte = reader.read()) != -1)
			{
				sb.append((char) oneByte);
			}
			return sb.toString();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public OutputStream setAsciiStream(long pos) throws SQLException
	{
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.WRITE);
		lo.seek64(pos - 1, LargeObject.SEEK_SET);
		return lo.getOutputStream();
	}

	@Override
	public Writer setCharacterStream(long pos) throws SQLException
	{
		OutputStream os = setAsciiStream(pos);
		return new OutputStreamWriter(os);
	}

	@Override
	public int setString(long pos, String str) throws SQLException
	{
		return setString(pos, str, 0, str.length());
	}

	@Override
	public int setString(long pos, String str, int offset, int len) throws SQLException
	{
		try
		{
			Writer writer = setCharacterStream(pos);
			try
			{
				writer.write(str, offset, len);
				return len;
			}
			finally
			{
				writer.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void truncate(long len) throws SQLException
	{
		LargeObject lo = largeObjectManager.open(oid, LargeObjectManager.WRITE);
		try
		{
			lo.truncate64(len);
		}
		finally
		{
			lo.close();
		}
	}
}
