package com.koch.ambeth.persistence.pg;

import org.postgresql.PGConnection;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.IUnmodifiedInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

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
