package com.koch.ambeth.persistence.jdbc.lob;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.io.OutputStream;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.IExtendedConnectionDialect;
import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.stream.chars.ICharacterInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputStream;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class LobStreamConverter implements IDedicatedConverter {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IExtendedConnectionDialect extendedConnectionDialect;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		try {
			if (Blob.class.isAssignableFrom(sourceType)) {
				return extendedConnectionDialect.createBinaryInputSource((Blob) value);
			}
			else if (Clob.class.isAssignableFrom(sourceType)) {
				return extendedConnectionDialect.createCharacterInputSource((Clob) value);
			}
			else if (IBinaryInputSource.class.isAssignableFrom(sourceType)) {
				Blob blob = connectionDialect.createBlob(connection);
				OutputStream os = blob.setBinaryStream(1);
				try {
					IBinaryInputStream is = ((IBinaryInputSource) value).deriveBinaryInputStream();
					try {
						int oneByte;
						while ((oneByte = is.readByte()) != -1) {
							os.write(oneByte);
						}
					}
					finally {
						is.close();
					}
				}
				finally {
					os.close();
				}
				return blob;
			}
			else if (ICharacterInputSource.class.isAssignableFrom(sourceType)) {
				Clob clob = connectionDialect.createClob(connection);
				Writer os = clob.setCharacterStream(1);
				try {
					ICharacterInputStream is = ((ICharacterInputSource) value).deriveCharacterInputStream();
					try {
						int oneChar;
						while ((oneChar = is.readChar()) != -1) {
							os.write(oneChar);
						}
					}
					finally {
						is.close();
					}
				}
				finally {
					os.close();
				}
				return clob;
			}
			throw new IllegalStateException("Must never happen");
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
