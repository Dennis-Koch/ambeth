package com.koch.ambeth.persistence.oracle;

/*-
 * #%L
 * jambeth-persistence-oracle11
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

import java.sql.Array;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import oracle.jdbc.OracleConnection;

public class OracleConnectionExtension implements IConnectionExtension {
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected Connection connection;

	protected final HashSet<Class<?>> numbersToConvert = new HashSet<>(
			Arrays.<Class<?>>asList(byte.class, Byte.class, Short.class, Integer.class, Long.class));

	@Override
	public Array createJDBCArray(Class<?> expectedComponentType, Object javaArray) {
		if (expectedComponentType == null) {
			expectedComponentType = javaArray.getClass().getComponentType();
		}
		if (Object.class.equals(expectedComponentType)) {
			Object firstItem = null;
			if (java.lang.reflect.Array.getLength(javaArray) > 0) {
				firstItem = java.lang.reflect.Array.get(javaArray, 0);
			}
			if (firstItem != null) {
				expectedComponentType = firstItem.getClass();
			}
		}

		if (numbersToConvert.contains(expectedComponentType)) {
			long[] longArray = new long[java.lang.reflect.Array.getLength(javaArray)];
			for (int i = longArray.length; i-- > 0;) {
				longArray[i] = ((Number) java.lang.reflect.Array.get(javaArray, i)).longValue();
			}
			javaArray = longArray;
		}
		else if (short.class.equals(expectedComponentType)
				&& Short.class.equals(javaArray.getClass().getComponentType())) {

			log.info("Oracle adapter does not support Short Java type, use primitive short");
			short[] shortArray = new short[java.lang.reflect.Array.getLength(javaArray)];
			for (int i = shortArray.length; i-- > 0;) {
				shortArray[i] = ((Number) java.lang.reflect.Array.get(javaArray, i)).shortValue();
			}
			javaArray = shortArray;
		}
		else if (expectedComponentType == char.class) {
			Character[] characterArray = new Character[java.lang.reflect.Array.getLength(javaArray)];
			for (int i = characterArray.length; i-- > 0;) {
				characterArray[i] = (Character) java.lang.reflect.Array.get(javaArray, i);
			}
			javaArray = characterArray;
		}

		String arrayTypeName = connectionDialect.getFieldTypeNameByComponentType(expectedComponentType);
		if (arrayTypeName == null) {
			throw new IllegalArgumentException(
					"Can not handle arrays of type " + expectedComponentType.getName());
		}
		try {
			return connection.unwrap(OracleConnection.class).createARRAY(arrayTypeName, javaArray);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
