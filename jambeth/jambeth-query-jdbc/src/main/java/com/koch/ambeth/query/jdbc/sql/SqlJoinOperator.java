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
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import jakarta.persistence.criteria.JoinType;

import java.util.List;
import java.util.Map;

public class SqlJoinOperator implements ISqlJoin, IInitializingBean {
    protected JoinType joinType = JoinType.LEFT;

    protected IOperand clause;

    protected String fullqualifiedEscapedTableName;

    protected String tableName;

    protected String tableAlias;

    protected IOperand joinedColumn;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(clause, "clause");
        ParamChecker.assertNotNull(tableName, "tableName");
        ParamChecker.assertFalse(tableName.isEmpty(), "tableName.isNotEmpty");
        ParamChecker.assertNotNull(fullqualifiedEscapedTableName, "fullqualifiedEscapedTableName");
        ParamChecker.assertFalse(fullqualifiedEscapedTableName.isEmpty(), "fullqualifiedEscapedTableName.isNotEmpty");
    }

    public void setClause(IOperand clause) {
        this.clause = clause;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    @Override
    public String getFullqualifiedEscapedTableName() {
        return fullqualifiedEscapedTableName;
    }

    public void setFullqualifiedEscapedTableName(String fullqualifiedEscapedTableName) {
        this.fullqualifiedEscapedTableName = fullqualifiedEscapedTableName;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    @Override
    public IOperand getJoinedColumn() {
        return joinedColumn;
    }

    public void setJoinedColumn(IOperand joinedColumn) {
        this.joinedColumn = joinedColumn;
    }

    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        operate(querySB, nameToValueMap, joinQuery, parameters);
    }

    @Override
    public void operate(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        if (!joinQuery) {
            throw new IllegalStateException("Join clause in non-join statement!");
        }

        switch (joinType) {
            case INNER:
                querySB.append("INNER");
                break;
            case LEFT:
                querySB.append("LEFT OUTER");
                break;
            case RIGHT:
                querySB.append("RIGHT OUTER");
                break;
            default:
                throw RuntimeExceptionUtil.createEnumNotSupportedException(joinType);
        }
        querySB.append(" JOIN ").append(fullqualifiedEscapedTableName).append(' ').append(getTableAlias()).append(" ON ");
        clause.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
    }
}
