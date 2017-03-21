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

public interface ISqlConnection
{
	void queueDelete(String tableName, CharSequence whereSql, List<Object> parameters);

	void queueDelete(String tableName, CharSequence[] whereSql);

	void queueDeleteAll(String tableName);

	void queueUpdate(String tableName, CharSequence valueAndNamesSql, CharSequence whereSql);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, List<Object> parameters);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, List<Object> parameters, String tableAlias);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, List<String> additionalSelectColumnList,
			CharSequence orderBySql, CharSequence limitSql, int offset, int length, List<Object> parameters);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, List<String> additionalSelectColumnList,
			CharSequence orderBySql, CharSequence limitSql, int offset, int length, List<Object> parameters, String tableAlias);

	IResultSet createResultSet(String tableName, String idFieldName, Class<?> idFieldType, String fieldsSQL, String additionalWhereSQL, List<?> ids);
}
