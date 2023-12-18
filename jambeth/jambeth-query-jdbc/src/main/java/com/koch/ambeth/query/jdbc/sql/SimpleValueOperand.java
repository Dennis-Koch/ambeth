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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.persistence.sql.ParamsUtil;
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleValueOperand implements IOperand, IValueOperand, IMultiValueOperand {
    public static final String P_PARAM_KEY = "ParamKey";

    public static final String P_TRY_ONLY = "TryOnly";

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected ListToSqlUtil listToSqlUtil;

    @Autowired
    protected IThreadLocalObjectCollector objectCollector;

    @Property
    protected Object paramKey;

    @Property(mandatory = false)
    protected boolean tryOnly;

    @Override
    public boolean isNull(Map<Object, Object> nameToValueMap) {
        return getValue(nameToValueMap) == null;
    }

    @Override
    public boolean isNullOrEmpty(Map<Object, Object> nameToValueMap) {
        var value = getValue(nameToValueMap);
        if (value == null) {
            return true;
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        } else if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        return "".equals(value);
    }

    protected Object getValueIntern(Map<Object, Object> nameToValueMap) {
        var value = nameToValueMap.get(paramKey);
        if (value == null) {
            if (!tryOnly && !nameToValueMap.containsKey(paramKey)) {
                throw new IllegalArgumentException("No entry for paramName '" + paramKey + "' found to expand query");
            }
        }
        return value;
    }

    @Override
    public Object getValue(Map<Object, Object> nameToValueMap) {
        var value = getValueIntern(nameToValueMap);
        return listToSqlUtil.extractValue(value, nameToValueMap);
    }

    @Override
    public List<Object> getMultiValue(Map<Object, Object> nameToValueMap) {
        var value = getValueIntern(nameToValueMap);
        var items = new ArrayList<>();
        listToSqlUtil.extractValueList(value, items, nameToValueMap);
        return items;
    }

    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        var value = getValue(nameToValueMap);
        var expectedTypeHint = (Class<?>) nameToValueMap.get(QueryConstants.EXPECTED_TYPE_HINT);
        if (expectedTypeHint != null) {
            value = conversionHelper.convertValueToType(expectedTypeHint, value);
        }
        if (parameters != null) {
            var preValue = (String) nameToValueMap.get(QueryConstants.PRE_VALUE_KEY);
            var postValue = (String) nameToValueMap.get(QueryConstants.POST_VALUE_KEY);
            if (preValue != null || postValue != null) {
                var tlObjectCollector = objectCollector.getCurrent();
                var sb = tlObjectCollector.create(StringBuilder.class);
                try {
                    if (preValue != null) {
                        sb.append(preValue);
                    }
                    if (value == null) {
                        sb.append("NULL");
                    } else {
                        sb.append(value);
                    }
                    if (postValue != null) {
                        sb.append(postValue);
                    }
                    value = sb.toString();
                } finally {
                    tlObjectCollector.dispose(sb);
                }
            }
            if (value != null) {
                ParamsUtil.addParam(parameters, value);
                querySB.append('?');
            } else {
                querySB.append("NULL");
            }
        } else {
            listToSqlUtil.expandValue(querySB, value, this, nameToValueMap);
        }
    }
}
