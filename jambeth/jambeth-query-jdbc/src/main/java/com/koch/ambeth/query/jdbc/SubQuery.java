package com.koch.ambeth.query.jdbc;

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

import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.query.ISubQuery;
import com.koch.ambeth.query.jdbc.sql.ITableAliasProvider;
import com.koch.ambeth.query.jdbc.sql.SqlJoinOperator;
import com.koch.ambeth.query.jdbc.sql.SqlSubselectOperand;
import com.koch.ambeth.util.IDisposable;

import java.util.List;
import java.util.Map;

public class SubQuery<T> implements ISubQuery<T>, ISubQueryIntern, IDisposable {
    protected final ISqlJoin[] joinOperands;

    protected final SqlSubselectOperand[] subQueries;

    protected final ISubQuery<T> subQuery;

    public SubQuery(ISubQuery<T> subQuery, ISqlJoin[] joinOperands, SqlSubselectOperand[] subQueries) {
        this.subQuery = subQuery;
        this.joinOperands = joinOperands;
        this.subQueries = subQueries;
    }

    @Override
    public void dispose() {
        subQuery.dispose();
    }

    @Override
    public Class<?> getEntityType() {
        return subQuery.getEntityType();
    }

    @Override
    public String getMainTableAlias() {
        return subQuery.getMainTableAlias();
    }

    @Override
    public void setMainTableAlias(String alias) {
        subQuery.setMainTableAlias(alias);
    }

    @Override
    public String[] getSqlParts(Map<Object, Object> nameToValueMap, List<Object> parameters, List<String> additionalSelectColumnList) {
        return subQuery.getSqlParts(nameToValueMap, parameters, additionalSelectColumnList);
    }

    @Override
    public void reAlias(ITableAliasProvider tableAliasProvider) {
        subQuery.setMainTableAlias(tableAliasProvider.getNextSubQueryAlias());

        for (var join : joinOperands) {
            if (join instanceof SqlJoinOperator) {
                var sqlJoin = (SqlJoinOperator) join;
                sqlJoin.setTableAlias(tableAliasProvider.getNextJoinAlias());
            }
        }
        for (var subselectOperand : subQueries) {
            var subQuery = subselectOperand.getSubQuery();
            if (subQuery instanceof SubQuery) {
                var subReference = (SubQuery<?>) subQuery;
                subReference.reAlias(tableAliasProvider);
            }
        }
    }
}
