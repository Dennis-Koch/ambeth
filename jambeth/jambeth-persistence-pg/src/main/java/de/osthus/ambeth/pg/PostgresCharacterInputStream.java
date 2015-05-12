package de.osthus.ambeth.pg;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;

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