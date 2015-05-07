package de.osthus.ambeth.persistence.jdbc.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.stream.binary.InputStreamToBinaryInputStream;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;
import de.osthus.ambeth.stream.chars.ReaderToCharacterInputSource;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@SQLStructure("PersistenceStream_structure.sql")
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/jdbc/stream/orm.xml") })
public class PersistenceStreamTest extends AbstractInformationBusWithPersistenceTest
{
	public static final String clobValue = "hallo";

	public static final byte[] blobValue = new byte[] { 1, 2, 3, 4, 5 };

	@Autowired
	protected ICache cache;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Before
	public void before() throws Throwable
	{
		Connection connection = connectionFactory.create();
		try
		{
			PreparedStatement pstm = connection.prepareStatement("INSERT INTO \"ENTITY_WITH_LOB\" (\"ID\", \"BLOB\", \"CLOB\", \"VERSION\") VALUES (?,?,?,?)");
			Blob blob = connectionDialect.createBlob(connection);
			{
				OutputStream os = blob.setBinaryStream(1);
				try
				{
					os.write(blobValue);
				}
				finally
				{
					os.close();
				}
			}
			Clob clob = connectionDialect.createClob(connection);
			{
				Writer os = clob.setCharacterStream(1);
				try
				{
					os.write(clobValue);
				}
				finally
				{
					os.close();
				}
			}
			pstm.setInt(1, 1);
			pstm.setBlob(2, blob);
			pstm.setClob(3, clob);
			pstm.setInt(4, 1);
			pstm.execute();
			connection.commit();
		}
		finally
		{
			connection.close();
		}
	}

	@Test
	public void blobRead()
	{
		EntityWithLob entity = cache.getObject(EntityWithLob.class, 1);
		IBinaryInputStream is = entity.getBlob().deriveBinaryInputStream();
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int oneByte;
			while ((oneByte = is.readByte()) != -1)
			{
				os.write(oneByte);
			}
			byte[] byteArray = os.toByteArray();
			Assert.assertArrayEquals(blobValue, byteArray);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Test
	public void blobAndClobWrite()
	{
		EntityWithLob entity = entityFactory.createEntity(EntityWithLob.class);
		entity.setBlob(new IBinaryInputSource()
		{
			@Override
			public IInputStream deriveInputStream()
			{
				return new InputStreamToBinaryInputStream(new ByteArrayInputStream(blobValue));
			}

			@Override
			public IBinaryInputStream deriveBinaryInputStream()
			{
				return new InputStreamToBinaryInputStream(new ByteArrayInputStream(blobValue));
			}
		});
		entity.setClob(new ReaderToCharacterInputSource(new StringReader(clobValue)));
		mergeProcess.process(entity, null, null, null);
	}

	@Test
	public void clobRead()
	{
		EntityWithLob entity = cache.getObject(EntityWithLob.class, 1);
		ICharacterInputStream is = entity.getClob().deriveCharacterInputStream();
		try
		{
			StringWriter sw = new StringWriter();
			int oneChar;
			while ((oneChar = is.readChar()) != -1)
			{
				sw.write(oneChar);
			}
			String value = sw.toString();
			Assert.assertEquals(clobValue, value);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}
}
