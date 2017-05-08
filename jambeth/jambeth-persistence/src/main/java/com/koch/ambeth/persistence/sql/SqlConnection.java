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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.OptimisticLockException;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.SelectPosition;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public abstract class SqlConnection implements ISqlConnection, IInitializingBean {
	// RegEx to add field aliases to paging subselects, e.g. S_A."ID":
	// outer sql: "S_A.ID"
	// inner sql: S_A."ID" AS "S_A.ID"
	private static final Pattern fieldWithAlias = Pattern.compile("(([SJ]_[A-Z]+\\.)\"([^\"]+)\")");
	private static final String innerFieldPattern = "$1 AS \"$2$3\"";
	private static final String outerFieldPattern = "\"$2$3\"";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
		for (int i = result.length; i-- > 0;) {
			if (result[i] == 0) {
				throw new OptimisticLockException("Object to delete has been altered");
			}
		}
	}

	@Override
	public void queueDelete(String tableName, CharSequence whereSql, List<Object> parameters) {
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder sb = objectCollector.create(AppendableStringBuilder.class);
		try {
			sb.append("DELETE FROM ");
			sqlBuilder.appendName(tableName, sb);
			sb.append(" WHERE ").append(whereSql);
			queueSqlExecute(sb.toString(), parameters);
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	@Override
	public void queueDelete(String tableName, CharSequence[] whereSql) {
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder sb = objectCollector.create(AppendableStringBuilder.class);
		String[] sqls = new String[whereSql.length];
		try {
			sb.append("DELETE FROM ");
			sqlBuilder.appendName(tableName, sb);
			sb.append(" WHERE ");
			String sqlBase = sb.toString();
			for (int i = whereSql.length; i-- > 0;) {
				sb.reset();
				sb.append(sqlBase).append(whereSql[i]);
				sqls[i] = sb.toString();
			}
			checkExecutionResult(queueSqlExecute(sqls));
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	@Override
	public void queueDeleteAll(String tableName) {
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder sb = objectCollector.create(AppendableStringBuilder.class);
		try {
			sb.append("DELETE FROM ");
			sqlBuilder.appendName(tableName, sb);
			queueSqlExecute(sb.toString(), null);
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	@Override
	public void queueUpdate(String tableName, CharSequence valueAndNamesSql, CharSequence whereSql) {
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder sb = objectCollector.create(AppendableStringBuilder.class);
		try {
			sb.append("UPDATE ");
			sqlBuilder.appendName(tableName, sb);
			sb.append(" SET ").append(valueAndNamesSql);
			if (whereSql != null && whereSql.length() > 0) {
				sb.append(" WHERE ").append(whereSql);
			}
			queueSqlExecute(sb.toString(), null);
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql,
			CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters) {
		return selectFields(tableName, fieldNamesSql, "", whereSql, orderBySql, limitSql, parameters);
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql,
			CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters) {
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectFields(tableName, fieldNamesSql, joinSql, whereSql, orderBySql, limitSql,
				parameters, tableAlias);
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql,
			CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters, String tableAlias) {
		boolean hasJoin = joinSql != null && joinSql.length() > 0;
		boolean hasWhere = whereSql != null && whereSql.length() > 0;
		boolean hasOrderBy = orderBySql != null && orderBySql.length() > 0;
		boolean hasLimit = limitSql != null && limitSql.length() > 0;
		boolean needsSubselectForLimit = false;
		SelectPosition limitPosition = connectionDialect.getLimitPosition();
		if (SelectPosition.AS_WHERE_CLAUSE.equals(limitPosition)) {
			needsSubselectForLimit = hasOrderBy && hasLimit;
		}
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder sb = objectCollector.create(AppendableStringBuilder.class);
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
			}
			else if (hasLimit) {
				switch (limitPosition) {
					case AS_WHERE_CLAUSE:
						if (!hasWhere) {
							sb.append(" WHERE ");
						}
						else {
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
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql,
			CharSequence whereSql, List<String> additionalSelectColumnList, CharSequence orderBySql,
			CharSequence limitSql, int offset, int length, List<Object> parameters) {
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectFields(tableName, fieldNamesSql, joinSql, whereSql, additionalSelectColumnList,
				orderBySql, limitSql, offset, length, parameters, tableAlias);
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql,
			CharSequence whereSql, List<String> additionalSelectColumnList, CharSequence orderBySql,
			CharSequence limitSql, int offset, int length, List<Object> parameters, String tableAlias) {
		boolean join = joinSql != null && joinSql.length() > 0;
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		AppendableStringBuilder sb = tlObjectCollector.create(AppendableStringBuilder.class);

		CharSequence outerFieldNamesSql, innerFieldNamesSql;
		if (tableAlias == null) {
			outerFieldNamesSql = fieldNamesSql;
			innerFieldNamesSql = fieldNamesSql;
		}
		else {
			Matcher fieldWithAliasMatcher = fieldWithAlias.matcher(fieldNamesSql);
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
					String additionalSelectColumn = additionalSelectColumnList.get(a);
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
		}
		finally {
			tlObjectCollector.dispose(sb);
		}
	}

	@Override
	public IResultSet createResultSet(final String tableName, final String idFieldName,
			final Class<?> idFieldType, final String fieldsSQL, final String additionalWhereSQL,
			List<?> ids) {
		if (ids == null || ids.size() == 0) {
			return EmptyResultSet.instance;
		}
		if (ids.size() <= maxInClauseBatchThreshold) {
			return createResultSetIntern(tableName, idFieldName, idFieldType, fieldsSQL,
					additionalWhereSQL, ids);
		}
		IList<IList<Object>> splitValues =
				persistenceHelper.splitValues(ids, maxInClauseBatchThreshold);

		ArrayList<IResultSetProvider> resultSetProviderStack = new ArrayList<>(splitValues.size());
		// Stack gets evaluated last->first so back iteration is correct to execute the sql in order
		// later
		for (int a = splitValues.size(); a-- > 0;) {
			final IList<Object> values = splitValues.get(a);
			resultSetProviderStack.add(new IResultSetProvider() {
				@Override
				public void skipResultSet() {
					// Intended blank
				}

				@Override
				public IResultSet getResultSet() {
					return createResultSetIntern(tableName, idFieldName, idFieldType, fieldsSQL,
							additionalWhereSQL, values);
				}
			});
		}
		CompositeResultSet compositeResultSet = new CompositeResultSet();
		compositeResultSet.setResultSetProviderStack(resultSetProviderStack);
		compositeResultSet.afterPropertiesSet();
		return compositeResultSet;
	}

	protected IResultSet createResultSetIntern(String tableName, String idFieldName,
			Class<?> idFieldType, String fieldsSQL, String additionalWhereSQL, List<?> ids) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		ArrayList<Object> parameters = new ArrayList<>();
		AppendableStringBuilder whereSB = tlObjectCollector.create(AppendableStringBuilder.class);
		try {
			persistenceHelper.appendSplittedValues(idFieldName, idFieldType, ids, parameters, whereSB);
			if (additionalWhereSQL != null) {
				whereSB.append(" AND ").append(additionalWhereSQL);
			}
			// if (forceOrder)
			// {
			// whereSB.Append(" ORDER BY ");
			// SqlBuilder.Append(idFieldName, whereSB);
			// }
			return selectFields(tableName, fieldsSQL, whereSB, null, null, parameters);
		}
		finally {
			tlObjectCollector.dispose(whereSB);
		}
	}

	protected abstract Object createArray(String tableName, String idFieldName, List<?> ids);

	protected abstract void disposeArray(Object array);

}
