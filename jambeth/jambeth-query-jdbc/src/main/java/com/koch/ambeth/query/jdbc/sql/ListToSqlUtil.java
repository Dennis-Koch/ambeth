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
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.OperandConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.appendable.IAppendable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ListToSqlUtil {
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected ISqlBuilder sqlBuilder;

    public void expandValue(IAppendable querySB, Object value, IOperand self, Map<Object, Object> nameToValueMap) {
        expandValue(querySB, value, self, nameToValueMap, null, null);
    }

    public void expandValue(IAppendable querySB, Object value, IOperand self, Map<Object, Object> nameToValueMap, String prefix, String suffix) {
        if (value instanceof List) {
            var list = (List<?>) value;
            for (int a = 0, size = list.size(); a < size; a++) {
                if (a > 0) {
                    querySB.append(',');
                }
                var item = list.get(a);
                if (prefix != null) {
                    querySB.append(prefix);
                }
                expandItem(querySB, item, self, nameToValueMap);
                if (suffix != null) {
                    querySB.append(suffix);
                }
            }
        } else if (value instanceof Collection) {
            var first = true;
            var iter = ((Collection<?>) value).iterator();
            while (iter.hasNext()) {
                var item = iter.next();
                if (first) {
                    first = false;
                } else {
                    querySB.append(',');
                }
                if (prefix != null) {
                    querySB.append(prefix);
                }
                expandItem(querySB, item, self, nameToValueMap);
                if (suffix != null) {
                    querySB.append(suffix);
                }
            }
        } else if (value != null && value.getClass().isArray()) {
            var size = Array.getLength(value);
            var preparedArrayGet = Arrays.prepareGet(value);
            for (int a = 0; a < size; a++) {
                var item = preparedArrayGet.get(a);
                if (a > 0) {
                    querySB.append(',');
                }
                if (prefix != null) {
                    querySB.append(prefix);
                }
                expandItem(querySB, item, self, nameToValueMap);
                if (suffix != null) {
                    querySB.append(suffix);
                }
            }
        } else {
            if (prefix != null) {
                querySB.append(prefix);
            }
            expandItem(querySB, value, self, nameToValueMap);
            if (suffix != null) {
                querySB.append(suffix);
            }
        }
    }

    public void extractValueList(Object value, List<Object> items) {
        extractValueList(value, items, (String) null);
    }

    protected void extractValueList(Object value, List<Object> items, String propertyName) {
        if (value instanceof List) {
            var list = (List<?>) value;
            for (int a = 0, size = list.size(); a < size; a++) {
                var item = list.get(a);
                extractValueList(item, items, propertyName);
            }
        } else if (value instanceof Collection) {
            var iter = ((Collection<?>) value).iterator();
            while (iter.hasNext()) {
                var item = iter.next();
                extractValueList(item, items, propertyName);
            }
        } else if (value != null && value.getClass().isArray()) {
            var size = Array.getLength(value);
            var preparedArrayGet = Arrays.prepareGet(value);
            for (int a = 0; a < size; a++) {
                var item = preparedArrayGet.get(a);
                extractValueList(item, items, propertyName);
            }
        } else {
            value = extractValue(value, propertyName);
            items.add(value);
        }
    }

    @SuppressWarnings("unchecked")
    public void extractValueList(Object value, List<Object> items, Map<Object, Object> nameToValueMap) {
        String propertyName = null;
        var propertyNameStack = (List<String>) nameToValueMap.get(OperandConstants.PropertyName);
        if (propertyNameStack != null) {
            propertyName = propertyNameStack.get(propertyNameStack.size() - 1);
        }
        extractValueList(value, items, propertyName);
    }

    @SuppressWarnings("unchecked")
    public Object extractValue(Object value, Map<Object, Object> nameToValueMap) {
        if (value == null) {
            return null;
        }
        String propertyName = null;
        var propertyNameStack = (List<String>) nameToValueMap.get(OperandConstants.PropertyName);
        if (propertyNameStack != null) {
            propertyName = propertyNameStack.get(propertyNameStack.size() - 1);
        }
        return extractValue(value, propertyName);
    }

    protected Object extractValue(Object value, String propertyName) {
        if (propertyName == null || value == null) {
            return value;
        }
        var valueMetaData = entityMetaDataProvider.getMetaData(value.getClass(), true);
        if (valueMetaData == null) {
            return value;
        }
        var member = valueMetaData.getMemberByName(propertyName);
        return member.getValue(value);
    }

    protected void expandItem(IAppendable querySB, Object value, IOperand self, Map<Object, Object> nameToValueMap) {
        if (value == null) {
            querySB.append("NULL");
        } else if (value instanceof String) {
            if (SqlEscapeHelper.escapeIfNecessary(self, nameToValueMap)) {
                querySB.append('\'');
            }
            sqlBuilder.escapeValue((String) value, querySB);
            if (SqlEscapeHelper.unescapeIfNecessary(self, nameToValueMap)) {
                querySB.append('\'');
            }
        } else {
            querySB.append(value.toString());
        }
    }
}
