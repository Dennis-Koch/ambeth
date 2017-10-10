package com.koch.ambeth.persistence.jdbc.stream;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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

import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.stream.binary.InputStreamToBinaryInputStream;
import com.koch.ambeth.stream.chars.ICharacterInputStream;
import com.koch.ambeth.stream.chars.ReaderToCharacterInputSource;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@SQLStructure("PersistenceStream_structure.sql")
@TestPropertiesList({@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/jdbc/stream/orm.xml")})
public class PersistenceStreamTest extends AbstractInformationBusWithPersistenceTest {
	public static final String clobValue = "hallo";

	public static final byte[] blobValue = new byte[] {1, 2, 3, 4, 5};

	@Autowired
	protected ICache cache;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Before
	public void before() throws Throwable {
		Connection connection = connectionFactory.create();
		try {
			PreparedStatement pstm = connection.prepareStatement(
					"INSERT INTO \"ENTITY_WITH_LOB\" (\"ID\", \"BLOB\", \"CLOB\", \"VERSION\") VALUES (?,?,?,?)");
			Blob blob = connectionDialect.createBlob(connection);
			{
				OutputStream os = blob.setBinaryStream(1);
				try {
					os.write(blobValue);
				}
				finally {
					os.close();
				}
			}
			Clob clob = connectionDialect.createClob(connection);
			{
				Writer os = clob.setCharacterStream(1);
				try {
					os.write(clobValue);
				}
				finally {
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
		finally {
			connection.close();
		}
	}

	@Test
	public void blobRead() {
		EntityWithLob entity = cache.getObject(EntityWithLob.class, 1);
		IBinaryInputStream is = entity.getBlob().deriveBinaryInputStream();
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int oneByte;
			while ((oneByte = is.readByte()) != -1) {
				os.write(oneByte);
			}
			byte[] byteArray = os.toByteArray();
			Assert.assertArrayEquals(blobValue, byteArray);
		}
		finally {
			try {
				is.close();
			}
			catch (IOException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Test
	public void blobAndClobWrite() {
		EntityWithLob entity = entityFactory.createEntity(EntityWithLob.class);
		entity.setBlob(new IBinaryInputSource() {
			@Override
			public IInputStream deriveInputStream() {
				return new InputStreamToBinaryInputStream(new ByteArrayInputStream(blobValue));
			}

			@Override
			public IBinaryInputStream deriveBinaryInputStream() {
				return new InputStreamToBinaryInputStream(new ByteArrayInputStream(blobValue));
			}
		});
		entity.setClob(new ReaderToCharacterInputSource(new StringReader(clobValue)));
		mergeProcess.process(entity);
	}

	@Test
	public void clobRead() {
		EntityWithLob entity = cache.getObject(EntityWithLob.class, 1);
		ICharacterInputStream is = entity.getClob().deriveCharacterInputStream();
		try {
			StringWriter sw = new StringWriter();
			int oneChar;
			while ((oneChar = is.readChar()) != -1) {
				sw.write(oneChar);
			}
			String value = sw.toString();
			Assert.assertEquals(clobValue, value);
		}
		finally {
			try {
				is.close();
			}
			catch (IOException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}
}
