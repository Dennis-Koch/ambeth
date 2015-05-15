package de.osthus.ambeth.persistence.jdbc;

import java.sql.Blob;
import java.sql.Clob;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IExtendedConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.lob.BlobInputSource;
import de.osthus.ambeth.persistence.jdbc.lob.ClobInputSource;
import de.osthus.ambeth.persistence.jdbc.lob.ILobInputSourceController;
import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputSource;

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
