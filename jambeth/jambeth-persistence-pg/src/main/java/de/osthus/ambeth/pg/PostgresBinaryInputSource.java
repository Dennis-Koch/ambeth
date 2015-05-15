package de.osthus.ambeth.pg;

import org.postgresql.PGConnection;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.IUnmodifiedInputSource;
import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.util.IImmutableType;

public class PostgresBinaryInputSource implements IBinaryInputSource, IUnmodifiedInputSource, IImmutableType
{
	protected long oid;

	protected PGConnection connection;

	public PostgresBinaryInputSource(long oid, PGConnection connection)
	{
		this.oid = oid;
		this.connection = connection;
	}

	@Override
	public IInputStream deriveInputStream()
	{
		try
		{
			return new PostgresBinaryInputStream(new PostgresBlob(connection, oid));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IBinaryInputStream deriveBinaryInputStream()
	{
		try
		{
			return new PostgresBinaryInputStream(new PostgresBlob(connection, oid));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
