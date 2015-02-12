package de.osthus.ambeth.persistence.jdbc.lob;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;

public class ClobInputStream implements ICharacterInputStream, IInputStream
{
	protected final Clob clob;

	protected final Reader reader;

	protected final IDataCursor cursor;

	public ClobInputStream(IDataCursor cursor, Clob clob)
	{
		this.cursor = cursor;
		this.clob = clob;
		try
		{
			reader = clob.getCharacterStream();
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
		cursor.dispose();
		try
		{
			clob.free();
		}
		catch (SQLException e)
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
