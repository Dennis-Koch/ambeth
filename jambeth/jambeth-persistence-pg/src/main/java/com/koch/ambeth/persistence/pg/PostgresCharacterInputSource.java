package com.koch.ambeth.persistence.pg;

import org.postgresql.PGConnection;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.IUnmodifiedInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputStream;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PostgresCharacterInputSource implements ICharacterInputSource, IUnmodifiedInputSource, IImmutableType
{
	protected long oid;

	protected PGConnection connection;

	public PostgresCharacterInputSource(long oid, PGConnection connection)
	{
		this.oid = oid;
		this.connection = connection;
	}

	@Override
	public IInputStream deriveInputStream()
	{
		try
		{
			System.out.println();
			return null;
			// return new PostgresCharacterInputStream(new PostgresClob(connection, oid));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public ICharacterInputStream deriveCharacterInputStream()
	{
		try
		{
			System.out.println();
			return null;
			// return new PostgresCharacterInputStream(new PostgresClob(connection, oid));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}