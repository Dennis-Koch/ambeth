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
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperatorAwareOperand;
import com.koch.ambeth.query.OperandConstants;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.List;
import java.util.Map;

public class SqlColumnOperand implements IOperand, IOperatorAwareOperand {
    @Property
    protected String columnName;

    @Property(mandatory = false)
    protected Class<?> entityType;

    @Property(mandatory = false)
    protected String propertyName;

    @Property(mandatory = false)
    protected Class<?> columnType;

    @Property(mandatory = false)
    protected Class<?> columnSubType;

    @Property(mandatory = false)
    protected SqlJoinOperator joinClause;

    @Autowired
    protected IConnectionDialect connectionDialect;

    @Autowired
    protected ITableAliasHolder tableAliasHolder;

    public Class<?> getColumnType() {
        return columnType;
    }

    public void setColumnType(Class<?> columnType) {
        this.columnType = columnType;
    }

    public Class<?> getColumnSubType() {
        return columnSubType;
    }

    public void setColumnSubType(Class<?> columnSubType) {
        this.columnSubType = columnSubType;
    }

    public void setJoinClause(SqlJoinOperator joinClause) {
        this.joinClause = joinClause;
    }

    public void setTableAliasHolder(ITableAliasHolder tableAliasHolder) {
        this.tableAliasHolder = tableAliasHolder;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void operatorStart(Map<Object, Object> nameToValueMap) {
        List<Class<?>> entityTypeStack = (List<Class<?>>) nameToValueMap.get(OperandConstants.EntityType);
        if (entityTypeStack == null) {
            entityTypeStack = new ArrayList<>();
            nameToValueMap.put(OperandConstants.EntityType, entityTypeStack);
        }
        entityTypeStack.add(entityType);

        List<String> propertyNameStack = (List<String>) nameToValueMap.get(OperandConstants.PropertyName);
        if (propertyNameStack == null) {
            propertyNameStack = new ArrayList<>();
            nameToValueMap.put(OperandConstants.PropertyName, propertyNameStack);
        }
        propertyNameStack.add(propertyName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void operatorEnd(Map<Object, Object> nameToValueMap) {
        List<Class<?>> entityTypeStack = (List<Class<?>>) nameToValueMap.get(OperandConstants.EntityType);
        entityTypeStack.remove(entityTypeStack.size() - 1);
        if (entityTypeStack.isEmpty()) {
            nameToValueMap.remove(OperandConstants.EntityType);
        }
        List<String> propertyNameStack = (List<String>) nameToValueMap.get(OperandConstants.PropertyName);
        propertyNameStack.remove(propertyNameStack.size() - 1);
        if (propertyNameStack.isEmpty()) {
            nameToValueMap.remove(OperandConstants.PropertyName);
        }
    }

    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        if (joinQuery || Boolean.TRUE.equals(nameToValueMap.get(QueryConstants.USE_TABLE_ALIAS))) {
            if (joinClause != null) {
                querySB.append(joinClause.getTableAlias());
            } else {
                querySB.append(tableAliasHolder.getTableAlias());
            }
            querySB.append('.');
        }
        connectionDialect.escapeName(columnName, querySB);
    }

    @Override
    public String toString() {
        if (tableAliasHolder == null) {
            return columnName;
        }
        String tableAlias = tableAliasHolder.getTableAlias();
        if (tableAlias == null) {
            return columnName;
        }
        return tableAlias + '.' + columnName;
    }
}
