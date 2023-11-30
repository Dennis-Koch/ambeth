package com.koch.ambeth.persistence.sql;

/*-
 * #%L
 * jambeth-persistence
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
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.SelectPosition;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import jakarta.persistence.OptimisticLockException;

import java.util.List;
import java.util.regex.Pattern;

public abstract class SqlConnection implements ISqlConnection, IInitializingBean {
    // RegEx to add field aliases to paging subselects, e.g. S_A."ID":
    // outer sql: "S_A.ID"
    // inner sql: S_A."ID" AS "S_A.ID"
    private static final Pattern fieldWithAlias = Pattern.compile("(([SJ]_[A-Z]+\\.)\"([^\"]+)\")");
    private static final String innerFieldPattern = "$1 AS \"$2$3\"";
    private static final String outerFieldPattern = "\"$2$3\"";

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IConnectionDialect connectionDialect;

    @Autowired
    protected IPersistenceHelper persistenceHelper;

    @Autowired
    protected ISqlBuilder sqlBuilder;

    @Autowired
    protected IThreadLocalObjectCollector objectCollector;

    protected int maxInClauseBatchThreshold;

    @Override
    public void afterPropertiesSet() throws Throwable {
        maxInClauseBatchThreshold = connectionDialect.getMaxInClauseBatchThreshold();
    }

    public void directSql(String sql) {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected void queueSqlExecute(String sql, List<Object> parameters) {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected int[] queueSqlExecute(String[] sql) {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected IResultSet sqlSelect(String sql, List<Object> parameters) {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected void checkExecutionResult(int[] result) {
        for (int i = result.length; i-- > 0; ) {
            if (result[i] == 0) {
                throw new OptimisticLockException("Object to delete has been altered");
            }
        }
    }

    @Override
    public void queueDelete(String tableName, CharSequence whereSql, List<Object> parameters) {
        var objectCollector = this.objectCollector.getCurrent();
        var sb = objectCollector.create(AppendableStringBuilder.class);
        try {
            sb.append("DELETE FROM ");
            sqlBuilder.appendName(tableName, sb);
            sb.append(" WHERE ").append(whereSql);
            queueSqlExecute(sb.toString(), parameters);
        } finally {
            objectCollector.dispose(sb);
        }
    }

    @Override
    public void queueDelete(String tableName, CharSequence[] whereSql) {
        var objectCollector = this.objectCollector.getCurrent();
        var sb = objectCollector.create(AppendableStringBuilder.class);
        var sqls = new String[whereSql.length];
        try {
            sb.append("DELETE FROM ");
            sqlBuilder.appendName(tableName, sb);
            sb.append(" WHERE ");
            var sqlBase = sb.toString();
            for (int i = whereSql.length; i-- > 0; ) {
                sb.reset();
                sb.append(sqlBase).append(whereSql[i]);
                sqls[i] = sb.toString();
            }
            checkExecutionResult(queueSqlExecute(sqls));
        } finally {
            objectCollector.dispose(sb);
        }
    }

    @Override
    public void queueDeleteAll(String tableName) {
        var objectCollector = this.objectCollector.getCurrent();
        var sb = objectCollector.create(AppendableStringBuilder.class);
        try {
            sb.append("DELETE FROM ");
            sqlBuilder.appendName(tableName, sb);
            queueSqlExecute(sb.toString(), null);
        } finally {
            objectCollector.dispose(sb);
        }
    }

    @Override
    public void queueUpdate(String tableName, CharSequence valueAndNamesSql, CharSequence whereSql) {
        var objectCollector = this.objectCollector.getCurrent();
        var sb = objectCollector.create(AppendableStringBuilder.class);
        try {
            sb.append("UPDATE ");
            sqlBuilder.appendName(tableName, sb);
            sb.append(" SET ").append(valueAndNamesSql);
            if (whereSql != null && whereSql.length() > 0) {
                sb.append(" WHERE ").append(whereSql);
            }
            queueSqlExecute(sb.toString(), null);
        } finally {
            objectCollector.dispose(sb);
        }
    }

    @Override
    public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql, List<Object> parameters) {
        return selectFields(tableName, fieldNamesSql, "", whereSql, orderBySql, limitSql, parameters);
    }

    @Override
    public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql, List<Object> parameters) {
        var join = joinSql != null && joinSql.length() > 0;
        var tableAlias = join ? "A" : null;
        return selectFields(tableName, fieldNamesSql, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias);
    }

    @Override
    public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql, List<Object> parameters,
            String tableAlias) {
        var hasJoin = joinSql != null && joinSql.length() > 0;
        var hasWhere = whereSql != null && whereSql.length() > 0;
        var hasOrderBy = orderBySql != null && orderBySql.length() > 0;
        var hasLimit = limitSql != null && limitSql.length() > 0;
        var needsSubselectForLimit = false;
        var limitPosition = connectionDialect.getLimitPosition();
        if (SelectPosition.AS_WHERE_CLAUSE.equals(limitPosition)) {
            needsSubselectForLimit = hasOrderBy && hasLimit;
        }
        var objectCollector = this.objectCollector.getCurrent();
        var sb = objectCollector.create(AppendableStringBuilder.class);
        try {
            if (needsSubselectForLimit) {
                // sub select needed for the rownum criteria
                sb.append("SELECT * FROM (");
            }
            sb.append("SELECT ");
            if (hasJoin) {
                sb.append("DISTINCT ");
            }
            sb.append(fieldNamesSql).append(" FROM ");
            sqlBuilder.appendName(tableName, sb);
            if (tableAlias != null) {
                sb.append(' ').append(tableAlias);
            }
            if (hasJoin) {
                sb.append(' ').append(joinSql);
            }
            if (hasWhere) {
                sb.append(" WHERE ").append(whereSql);
            }
            if (hasOrderBy) {
                sb.append(" ").append(orderBySql);
            }
            if (needsSubselectForLimit) {
                sb.append(") WHERE ").append(limitSql);
            } else if (hasLimit) {
                switch (limitPosition) {
                    case AS_WHERE_CLAUSE:
                        if (!hasWhere) {
                            sb.append(" WHERE ");
                        } else {
                            sb.append(" AND ");
                        }
                        break;
                    case AFTER_WHERE:
                        if (hasOrderBy) {
                            sb.append(" ");
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("'SELECT TOP' not supported yet");
                }
                sb.append(limitSql);
            }
            return sqlSelect(sb.toString(), parameters);
        } finally {
            objectCollector.dispose(sb);
        }
    }

    @Override
    public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, List<String> additionalSelectColumnList, CharSequence orderBySql,
            CharSequence limitSql, int offset, int length, List<Object> parameters) {
        var join = joinSql != null && joinSql.length() > 0;
        var tableAlias = join ? "A" : null;
        return selectFields(tableName, fieldNamesSql, joinSql, whereSql, additionalSelectColumnList, orderBySql, limitSql, offset, length, parameters, tableAlias);
    }

    @Override
    public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, List<String> additionalSelectColumnList, CharSequence orderBySql,
            CharSequence limitSql, int offset, int length, List<Object> parameters, String tableAlias) {
        var join = joinSql != null && joinSql.length() > 0;
        var tlObjectCollector = objectCollector.getCurrent();

        var sb = tlObjectCollector.create(AppendableStringBuilder.class);

        CharSequence outerFieldNamesSql, innerFieldNamesSql;
        if (tableAlias == null) {
            outerFieldNamesSql = fieldNamesSql;
            innerFieldNamesSql = fieldNamesSql;
        } else {
            var fieldWithAliasMatcher = fieldWithAlias.matcher(fieldNamesSql);
            outerFieldNamesSql = fieldWithAliasMatcher.replaceAll(outerFieldPattern);
            innerFieldNamesSql = fieldWithAliasMatcher.replaceAll(innerFieldPattern);
        }
        try {
            sb.append("SELECT ").append(outerFieldNamesSql).append(" FROM (SELECT");
            if (join) {
                sb.append(" DISTINCT");
            }
            sb.append(" ROW_NUMBER() OVER");
            if (orderBySql != null && orderBySql.length() > 0) {
                sb.append(" (").append(orderBySql).append(")");
            }
            sb.append(" AS rn");
            if (innerFieldNamesSql.length() > 0) {
                sb.append(',').append(innerFieldNamesSql);
            }

            if (additionalSelectColumnList != null) {
                for (int a = 0, size = additionalSelectColumnList.size(); a < size; a++) {
                    var additionalSelectColumn = additionalSelectColumnList.get(a);
                    // additionalSelectColumn is expected to be already escaped at this point. No need to
                    // double escape
                    sb.append(',').append(additionalSelectColumn);
                }
            }
            sb.append(" FROM ");
            sqlBuilder.appendName(tableName, sb);
            if (tableAlias != null) {
                sb.append(" ").append(tableAlias).append(" ").append(joinSql);
            }
            if (whereSql != null && whereSql.length() > 0) {
                sb.append(" WHERE ").append(whereSql);
            }
            sb.append(") rnSelect WHERE rn>? AND rn<=?");
            if (orderBySql != null && orderBySql.length() > 0) {
                sb.append(" ORDER BY rn ASC");
            }

            ParamsUtil.addParam(parameters, offset);
            ParamsUtil.addParam(parameters, offset + length);

            return sqlSelect(sb.toString(), parameters);
        } finally {
            tlObjectCollector.dispose(sb);
        }
    }

    @Override
    public IResultSet createResultSet(String tableName, CharSequence fieldsSql, CharSequence additionalWhereSql, CharSequence orderBySql, IdContainer idContainer) {
        var amountOfIds = idContainer.getAmountOfIds();
        if (amountOfIds == 0) {
            return EmptyResultSet.instance;
        }
        if (amountOfIds <= maxInClauseBatchThreshold) {
            return createResultSetIntern(tableName, fieldsSql, additionalWhereSql, orderBySql, idContainer);
        }
        var idsInChunks = persistenceHelper.splitValues(idContainer.getValues(), maxInClauseBatchThreshold);
        var resultSetProviderStack = new ArrayList<IResultSetProvider>(idsInChunks.size());
        // Stack gets evaluated last->first so back iteration is correct to execute the sql in order later
        var unmod_fieldsSql = fieldsSql != null ? fieldsSql.toString() : null;
        var unmod_additionalWhereSql = additionalWhereSql != null ? additionalWhereSql.toString() : null;
        var unmod_orderBySql = orderBySql != null ? orderBySql.toString() : null;
        for (int chunkIndex = idsInChunks.size(); chunkIndex-- > 0; ) {
            var idsOfChunk = idsInChunks.get(chunkIndex);
            var idContainerOfChunk = IdContainerImpl.ofChunk(idContainer, idsOfChunk);
            resultSetProviderStack.add(() -> createResultSetIntern(tableName, unmod_fieldsSql, unmod_additionalWhereSql, unmod_orderBySql, idContainerOfChunk));
        }
        var compositeResultSet = new CompositeResultSet();
        compositeResultSet.setResultSetProviderStack(resultSetProviderStack);
        compositeResultSet.afterPropertiesSet();
        return compositeResultSet;
    }

    protected IResultSet createResultSetIntern(String tableName, CharSequence fieldsSQL, CharSequence additionalWhereSQL, CharSequence orderBySQL, IdContainer idContainer) {
        var objectCollector = this.objectCollector.getCurrent();
        var parameters = new ArrayList<>();
        var whereSB = objectCollector.create(AppendableStringBuilder.class);
        try {
            persistenceHelper.appendSplittedValues(idContainer, parameters, whereSB);
            if (additionalWhereSQL != null) {
                whereSB.append(" AND ").append(additionalWhereSQL);
            }
            return selectFields(tableName, fieldsSQL, whereSB, orderBySQL, null, parameters);
        } finally {
            objectCollector.dispose(whereSB);
        }
    }

    protected abstract Object createArray(String tableName, String idFieldName, List<?> ids);

    protected abstract void disposeArray(Object array);

}
