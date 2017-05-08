package com.koch.ambeth.persistence.jdbc.array;

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

import java.sql.Array;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ArrayConverter implements IDedicatedConverter {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionExtension connectionExtension;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		try {
			if (Array.class.isAssignableFrom(sourceType)) {
				Array array = (Array) value;

				ArrayList<Object> list = new ArrayList<>();
				ResultSet rs = array.getResultSet();
				try {
					Class<?> componentType = null;
					if (expectedType.isArray()) {
						componentType = expectedType.getComponentType();
					}
					else if (Collection.class.isAssignableFrom(expectedType)
							&& additionalInformation != null) {
						componentType = (Class<?>) additionalInformation;
					}
					while (rs.next()) {
						int index = ((Number) rs.getObject(1)).intValue();
						Object item = rs.getObject(2);
						while (list.size() < index) {
							list.add(null);
						}
						item = conversionHelper.convertValueToType(componentType, item);
						list.set(index - 1, item);
					}
					if (expectedType.isArray()) {
						Object targetArray = java.lang.reflect.Array.newInstance(componentType, list.size());
						for (int a = 0, size = list.size(); a < size; a++) {
							java.lang.reflect.Array.set(targetArray, a, list.get(a));
						}
						return targetArray;
					}
					else if (Set.class.isAssignableFrom(expectedType)) {
						Set<Object> result =
								new java.util.HashSet<>((int) ((list.size() + 1) / 0.75f), 0.75f);
						result.addAll(list);
						return result;
					}
					else if (Collection.class.isAssignableFrom(expectedType)) {
						java.util.ArrayList<Object> result = new java.util.ArrayList<>(list.size());
						result.addAll(list);
						return result;
					}
				}
				finally {
					JdbcUtil.close(rs);
				}
			}
			else if (sourceType.isArray()) {
				if (Array.class.isAssignableFrom(expectedType)) {
					ParamChecker.assertParamNotNull(additionalInformation, "additionalInformation");
					Class<?> componentType = (Class<?>) additionalInformation;
					return connectionExtension.createJDBCArray(componentType, value);
				}
				else if (Set.class.isAssignableFrom(expectedType)) {
					Set<?> result = new java.util.HashSet<>(Arrays.asList(value));
					return result;
				}
			}
			else if (Collection.class.isAssignableFrom(sourceType)) {
				if (Array.class.isAssignableFrom(expectedType)) {
					ParamChecker.assertParamNotNull(additionalInformation, "additionalInformation");

					Object[] valueArray = ((Collection<?>) value).toArray();
					Class<?> componentType = (Class<?>) additionalInformation;
					return connectionExtension.createJDBCArray(componentType, valueArray);
				}
			}
			throw new IllegalArgumentException("Cannot convert from '" + sourceType + "' to '"
					+ expectedType
					+ "'. This is a bug if I get called for types which I do not support and I did not register with!");
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
