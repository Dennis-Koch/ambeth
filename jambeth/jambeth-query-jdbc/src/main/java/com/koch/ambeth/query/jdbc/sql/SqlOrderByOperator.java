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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SqlOrderByOperator implements IOperator, IInitializingBean {
    protected static final Pattern ignoredColumnNamesPattern = Pattern.compile("([A-Z_]+\\.)?\"?(ID|VERSION)\"?");

    protected IOperand column;

    protected IThreadLocalObjectCollector objectCollector;

    protected OrderByType orderByType;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(column, "column");
        ParamChecker.assertNotNull(objectCollector, "objectCollector");
        ParamChecker.assertNotNull(orderByType, "orderByType");
    }

    public void setColumn(IOperand column) {
        this.column = column;
    }

    public void setObjectCollector(IThreadLocalObjectCollector objectCollector) {
        this.objectCollector = objectCollector;
    }

    public void setOrderByType(OrderByType orderByType) {
        this.orderByType = orderByType;
    }

    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
        operate(querySB, nameToValueMap, joinQuery, parameters);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void operate(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
        var firstOrderByState = (Boolean) nameToValueMap.get(QueryConstants.FIRST_ORDER_BY_STATE);
        var additionalSelectColumnList = (List<String>) nameToValueMap.get(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
        if (firstOrderByState == null || Boolean.TRUE.equals(firstOrderByState)) {
            nameToValueMap.put(QueryConstants.FIRST_ORDER_BY_STATE, Boolean.FALSE);
            querySB.append(" ORDER BY ");
        } else {
            querySB.append(',');
        }
        if (additionalSelectColumnList != null) {
            var tlObjectCollector = objectCollector.getCurrent();
            var sb = tlObjectCollector.create(AppendableStringBuilder.class);
            try {
                column.expandQuery(sb, nameToValueMap, joinQuery, parameters);
                var matcher = ignoredColumnNamesPattern.matcher(sb);
                var matches = matcher.matches();
                var tableAlias = matches ? matcher.group(1) : null;
                if (!matches || (tableAlias != null && !tableAlias.startsWith("S_"))) {
                    additionalSelectColumnList.add(sb.toString());
                }
                column.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
            } finally {
                tlObjectCollector.dispose(sb);
            }
        } else {
            column.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
        }
        switch (orderByType) {
            case ASC:
                querySB.append(" ASC");
                break;
            case DESC:
                querySB.append(" DESC");
                break;
            default:
                throw new IllegalStateException("Type " + orderByType + " not supported");
        }
    }
}
