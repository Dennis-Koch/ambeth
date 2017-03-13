package com.koch.ambeth.persistence.jdbc.lob;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BlobInputStream implements IBinaryInputStream, IInputStream
{
	protected final Blob blob;

	protected final InputStream is;

	protected final IDataCursor cursor;

	public BlobInputStream(IDataCursor cursor, Blob blob)
	{
		this.cursor = cursor;
		this.blob = blob;
		try
		{
			is = blob.getBinaryStream();
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			is.close();
			blob.free();
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			cursor.dispose();
		}
	}

	@Override
	public int readByte()
	{
		try
		{
			return is.read();
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
