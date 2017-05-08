package com.koch.ambeth.persistence.pg;

/*-
 * #%L
 * jambeth-persistence-pg
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PostgresConnectionExtension implements IConnectionExtension {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

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
		String[] strings = PostgresDialect.typeToArrayTypeNameMap.get(expectedComponentType);
		try {
			return connection.createArrayOf(strings[1], (Object[]) javaArray);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
