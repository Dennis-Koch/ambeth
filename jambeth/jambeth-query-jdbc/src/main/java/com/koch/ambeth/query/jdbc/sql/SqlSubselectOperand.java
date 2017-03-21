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
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.ISubQuery;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SqlSubselectOperand implements IOperand, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ISubQuery<?> subQuery;

	protected SqlColumnOperand[] selectedColumns;

	protected IDatabaseMetaData databaseMetaData;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(subQuery, "subQuery");
		ParamChecker.assertNotNull(selectedColumns, "selectedColumns");

		ParamChecker.assertNotNull(databaseMetaData, "database");
	}

	public void setSubQuery(ISubQuery<?> subQuery) {
		this.subQuery = subQuery;
	}

	public void setSelectedColumns(SqlColumnOperand[] selectedColumns) {
		this.selectedColumns = selectedColumns;
	}

	public void setDatabase(IDatabaseMetaData databaseMetaData) {
		this.databaseMetaData = databaseMetaData;
	}

	public ISubQuery<?> getSubQuery() {
		return subQuery;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		Class<?> entityType = subQuery.getEntityType();
		ITableMetaData table = databaseMetaData.getTableByType(entityType);
		String tableName = table.getFullqualifiedEscapedName();
		String tableAlias = subQuery.getMainTableAlias();

		String[] sqlParts =
				subQuery.getSqlParts(nameToValueMap, parameters, EmptyList.<String>getInstance());
		String joinSql = sqlParts[0];
		String whereSql = sqlParts[1];
		String orderBySql = sqlParts[2];
		String limitSql = sqlParts[3];

		querySB.append("SELECT ");
		boolean firstColumn = true;
		SqlColumnOperand[] selectedColumns = this.selectedColumns;
		for (int i = 0, size = selectedColumns.length; i < size; i++) {
			if (firstColumn) {
				firstColumn = false;
			}
			else {
				querySB.append(',');
			}
			SqlColumnOperand column = selectedColumns[i];
			querySB.append(tableAlias).append(".").append('"').append(column.columnName).append('"');
		}
		querySB.append(" FROM ").append(tableName).append(" ").append(tableAlias);
		if (joinSql != null && !joinSql.isEmpty()) {
			querySB.append(" ").append(joinSql);
		}
		if (whereSql != null && !whereSql.isEmpty()) {
			querySB.append(" WHERE ").append(whereSql);
		}
		if (orderBySql != null && !orderBySql.isEmpty()) {
			querySB.append(" ").append(orderBySql);
		}
		if (limitSql != null && !limitSql.isEmpty()) {
			querySB.append(" ").append(limitSql);
		}
	}
}
