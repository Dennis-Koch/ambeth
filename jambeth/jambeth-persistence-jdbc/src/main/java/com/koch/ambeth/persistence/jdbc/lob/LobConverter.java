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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class LobConverter implements IDedicatedConverter {
	@Autowired
	protected Connection connection;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		try {
			if (Blob.class.isAssignableFrom(sourceType)) {
				Blob blob = (Blob) value;

				byte[] array;
				int length = (int) blob.length();
				if (length == 0) {
					array = new byte[0];
				}
				else {
					InputStream is = blob.getBinaryStream();
					try {
						array = new byte[length];

						int bytesRead;
						int index = 0;
						while ((bytesRead = is.read(array, index, length - index)) != -1) {
							index += bytesRead;
							if (index == length) {
								break;
							}
						}
					}
					finally {
						is.close();
					}
				}
				if (byte[].class.equals(expectedType)) {
					return array;
				}
				else if (String.class.equals(expectedType)) {
					return new String(array, Properties.CHARSET_UTF_8);
				}
			}
			else if (Clob.class.isAssignableFrom(sourceType)) {
				Clob clob = (Clob) value;

				int length = (int) clob.length();
				char[] array;
				if (length == 0) {
					array = new char[0];
				}
				else {
					Reader is = clob.getCharacterStream();
					try {
						array = new char[length];

						int bytesRead;
						int index = 0;
						while ((bytesRead = is.read(array, index, length - index)) != -1) {
							index += bytesRead;
							if (index == length) {
								break;
							}
						}
						if (array.length > index) {
							char[] newArray = new char[index];
							System.arraycopy(array, 0, newArray, 0, newArray.length);
							array = newArray;
						}
					}
					finally {
						is.close();
					}
				}
				if (char[].class.equals(expectedType)) {
					return array;
				}
				else if (String.class.equals(expectedType)) {
					if (array.length == 0) {
						return "";
					}
					return new String(array);
				}
			}
			else if (byte[].class.isAssignableFrom(sourceType)) {
				if (Blob.class.isAssignableFrom(expectedType)) {
					Blob blob = connectionDialect.createBlob(connection);
					OutputStream os = blob.setBinaryStream(1);
					try {
						os.write((byte[]) value);
					}
					finally {
						os.close();
					}
					return blob;
				}
			}
			else if (char[].class.isAssignableFrom(sourceType)) {
				if (Clob.class.isAssignableFrom(expectedType)) {
					Clob clob = connectionDialect.createClob(connection);
					IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
					StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
					Writer writer = clob.setCharacterStream(1);
					try {
						sb.append((char[]) value);
						writer.append(sb);
						return clob;
					}
					finally {
						writer.close();
						tlObjectCollector.dispose(sb);
					}
				}
			}
			else if (CharSequence.class.isAssignableFrom(sourceType)) {
				if (Clob.class.isAssignableFrom(expectedType)) {
					Clob clob = connectionDialect.createClob(connection);
					Writer writer = clob.setCharacterStream(1);
					try {
						writer.append((CharSequence) value);
					}
					finally {
						writer.close();
					}
					return clob;
				}
			}
			throw new IllegalArgumentException("Cannot convert from '" + sourceType + "' to '"
					+ expectedType
					+ "'. This is a bug if I get called for types which I do not support and I did not register with!");
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
