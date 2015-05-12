package de.osthus.ambeth.pg;

import org.postgresql.PGConnection;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.IUnmodifiedInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;
import de.osthus.ambeth.util.IImmutableType;

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