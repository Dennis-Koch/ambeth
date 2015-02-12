package de.osthus.ambeth.persistence.jdbc.lob;

import java.io.OutputStream;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.stream.chars.ICharacterInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;
import de.osthus.ambeth.util.IDedicatedConverter;

public class LobStreamConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected ILobInputSourceController lobInputSourceController;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		try
		{
			if (Blob.class.isAssignableFrom(sourceType))
			{
				return new BlobInputSource(lobInputSourceController);
			}
			else if (Clob.class.isAssignableFrom(sourceType))
			{
				return new ClobInputSource(lobInputSourceController);
			}
			else if (IBinaryInputSource.class.isAssignableFrom(sourceType))
			{
				Blob blob = connection.createBlob();
				OutputStream os = blob.setBinaryStream(1);
				try
				{
					IBinaryInputStream is = ((IBinaryInputSource) value).deriveBinaryInputStream();
					try
					{
						int oneByte;
						while ((oneByte = is.readByte()) != -1)
						{
							os.write(oneByte);
						}
					}
					finally
					{
						is.close();
					}
				}
				finally
				{
					os.close();
				}
				return blob;
			}
			else if (IBinaryInputSource.class.isAssignableFrom(sourceType))
			{
				Clob clob = connection.createClob();
				Writer os = clob.setCharacterStream(1);
				try
				{
					ICharacterInputStream is = ((ICharacterInputSource) value).deriveCharacterInputStream();
					try
					{
						int oneChar;
						while ((oneChar = is.readChar()) != -1)
						{
							os.write(oneChar);
						}
					}
					finally
					{
						is.close();
					}
				}
				finally
				{
					os.close();
				}
				return clob;
			}
			throw new IllegalStateException("Must never happen");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
