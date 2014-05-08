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
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IDedicatedConverter;
import de.osthus.ambeth.util.ParamChecker;

public class LobConversionHelper implements IDedicatedConverter, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(LobConversionHelper.class)
	private ILogger log;

	protected Connection connection;

	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(connection, "connection");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

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
			else if (byte[].class.isAssignableFrom(sourceType))
			{
				if (Blob.class.isAssignableFrom(expectedType))
				{
					Blob blob = connection.createBlob();
					OutputStream os = blob.setBinaryStream(0);
					os.write((byte[]) value);
					os.close();
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
					Writer writer = clob.setCharacterStream(0);
					try
					{
						sb.append((char[]) value);
						writer.append(sb);
						writer.flush();
						return clob;
					}
					finally
					{
						tlObjectCollector.dispose(sb);
					}
				}
			}
			else if (String.class.isAssignableFrom(sourceType))
			{
				if (Clob.class.isAssignableFrom(expectedType))
				{
					Clob clob = connection.createClob();
					IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
					StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
					Writer writer = clob.setCharacterStream(0);
					try
					{
						sb.append((String) value);
						writer.append(sb);
						writer.flush();
						return clob;
					}
					finally
					{
						tlObjectCollector.dispose(sb);
					}
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
