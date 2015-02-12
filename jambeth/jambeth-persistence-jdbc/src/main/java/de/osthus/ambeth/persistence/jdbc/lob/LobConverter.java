package de.osthus.ambeth.persistence.jdbc.lob;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IDedicatedConverter;

public class LobConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		try
		{
			if (Blob.class.isAssignableFrom(sourceType))
			{
				Blob blob = (Blob) value;

				InputStream is = blob.getBinaryStream();
				int length = (int) blob.length();
				byte[] array = new byte[length];
				int bytesRead = is.read(array, 0, length);
				if (bytesRead < length)
				{
					int index = bytesRead;
					byte[] oneByte = new byte[1];
					while ((bytesRead = is.read(oneByte, index, 1)) != -1)
					{
						array[index] = oneByte[0];
						index += bytesRead;
					}
				}
				if (byte[].class.equals(expectedType))
				{
					return array;
				}
				else if (String.class.equals(expectedType))
				{
					return new String(array, Properties.CHARSET_UTF_8);
				}
			}
			else if (Clob.class.isAssignableFrom(sourceType))
			{
				Clob clob = (Clob) value;

				Reader reader = clob.getCharacterStream();
				try
				{
					int length = (int) clob.length();
					char[] array = new char[length];
					int bytesRead = reader.read(array, 0, length);
					if (bytesRead < length)
					{
						int index = bytesRead;
						char[] oneChar = new char[1];
						while ((bytesRead = reader.read(oneChar, index, 1)) != -1)
						{
							array[index] = oneChar[0];
							index += bytesRead;
						}
					}
					if (char[].class.equals(expectedType))
					{
						return array;
					}
					else if (String.class.equals(expectedType))
					{
						return new String(array);
					}
				}
				finally
				{
					reader.close();
				}
			}
			else if (byte[].class.isAssignableFrom(sourceType))
			{
				if (Blob.class.isAssignableFrom(expectedType))
				{
					Blob blob = connection.createBlob();
					OutputStream os = blob.setBinaryStream(1);
					try
					{
						os.write((byte[]) value);
					}
					finally
					{
						os.close();
					}
					return blob;
				}
			}
			else if (char[].class.isAssignableFrom(sourceType))
			{
				if (Clob.class.isAssignableFrom(expectedType))
				{
					Clob clob = connection.createClob();
					IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
					StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
					Writer writer = clob.setCharacterStream(1);
					try
					{
						sb.append((char[]) value);
						writer.append(sb);
						return clob;
					}
					finally
					{
						writer.close();
						tlObjectCollector.dispose(sb);
					}
				}
			}
			else if (CharSequence.class.isAssignableFrom(sourceType))
			{
				if (Clob.class.isAssignableFrom(expectedType))
				{
					Clob clob = connection.createClob();
					Writer writer = clob.setCharacterStream(1);
					try
					{
						writer.append((CharSequence) value);
					}
					finally
					{
						writer.close();
					}
					return clob;
				}
			}
			throw new IllegalArgumentException("Cannot convert from '" + sourceType + "' to '" + expectedType
					+ "'. This is a bug if I get called for types which I do not support and I did not register with!");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
