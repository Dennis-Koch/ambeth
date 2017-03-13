package com.koch.ambeth.persistence.pg;

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
