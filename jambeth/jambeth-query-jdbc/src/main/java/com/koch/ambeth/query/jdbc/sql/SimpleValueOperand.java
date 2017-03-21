package com.koch.ambeth.query.jdbc.sql;

/*-
 * #%L
 * jambeth-query-jdbc
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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.persistence.sql.ParamsUtil;
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class SimpleValueOperand implements IOperand, IValueOperand, IMultiValueOperand {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected ListToSqlUtil listToSqlUtil;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Property
	protected String paramName;

	@Property(mandatory = false)
	protected boolean tryOnly;

	@Override
	public boolean isNull(Map<Object, Object> nameToValueMap) {
		return getValue(nameToValueMap) == null;
	}

	@Override
	public boolean isNullOrEmpty(Map<Object, Object> nameToValueMap) {
		Object value = getValue(nameToValueMap);
		if (value == null) {
			return true;
		}
		else if (value instanceof Collection) {
			return ((Collection<?>) value).isEmpty();
		}
		else if (value.getClass().isArray()) {
			return Array.getLength(value) == 0;
		}
		return "".equals(value);
	}

	protected Object getValueIntern(Map<Object, Object> nameToValueMap) {
		Object value = nameToValueMap.get(paramName);
		if (value == null) {
			if (!tryOnly && !nameToValueMap.containsKey(paramName)) {
				throw new IllegalArgumentException(
						"No entry for paramName '" + paramName + "' found to expand query");
			}
		}
		return value;
	}

	@Override
	public Object getValue(Map<Object, Object> nameToValueMap) {
		Object value = getValueIntern(nameToValueMap);
		return listToSqlUtil.extractValue(value, nameToValueMap);
	}

	@Override
	public IList<Object> getMultiValue(Map<Object, Object> nameToValueMap) {
		Object value = getValueIntern(nameToValueMap);
		ArrayList<Object> items = new ArrayList<>();
		listToSqlUtil.extractValueList(value, items, nameToValueMap);
		return items;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		Object value = getValue(nameToValueMap);
		Class<?> expectedTypeHint = (Class<?>) nameToValueMap.get(QueryConstants.EXPECTED_TYPE_HINT);
		if (expectedTypeHint != null) {
			value = conversionHelper.convertValueToType(expectedTypeHint, value);
		}
		if (parameters != null) {
			String preValue = (String) nameToValueMap.get(QueryConstants.PRE_VALUE_KEY);
			String postValue = (String) nameToValueMap.get(QueryConstants.POST_VALUE_KEY);
			if (preValue != null || postValue != null) {
				IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
				StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
				if (preValue != null) {
					sb.append(preValue);
				}
				if (value == null) {
					sb.append("NULL");
				}
				else {
					sb.append(value);
				}
				if (postValue != null) {
					sb.append(postValue);
				}
				value = sb.toString();
				tlObjectCollector.dispose(sb);
			}
			if (value != null) {
				ParamsUtil.addParam(parameters, value);
				querySB.append('?');
			}
			else {
				querySB.append("NULL");
			}
		}
		else {
			listToSqlUtil.expandValue(querySB, value, this, nameToValueMap);
		}
	}
}
