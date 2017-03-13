package com.koch.ambeth.persistence.jdbc;

import java.sql.Blob;
import java.sql.Clob;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IExtendedConnectionDialect;
import com.koch.ambeth.persistence.jdbc.lob.BlobInputSource;
import com.koch.ambeth.persistence.jdbc.lob.ClobInputSource;
import com.koch.ambeth.persistence.jdbc.lob.ILobInputSourceController;
import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputSource;

public class AbstractExtendedConnectionDialect implements IExtendedConnectionDialect
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ILobInputSourceController lobInputSourceController;

	@Override
	public IBinaryInputSource createBinaryInputSource(Blob blob)
	{
		return new BlobInputSource(lobInputSourceController);
	}

	@Override
	public ICharacterInputSource createCharacterInputSource(Clob clob)
	{
		return new ClobInputSource(lobInputSourceController);
	}
}
