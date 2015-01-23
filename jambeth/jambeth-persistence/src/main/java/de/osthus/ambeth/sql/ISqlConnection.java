package de.osthus.ambeth.sql;

import java.util.List;

public interface ISqlConnection
{
	void queueDelete(String tableName, String whereSql, List<Object> parameters);

	void queueDelete(String tableName, String[] whereSql);

	void queueDeleteAll(String tableName);

	void queueUpdate(String tableName, String valueAndNamesSql, String whereSql);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence whereSql, CharSequence limitSql, List<Object> parameters);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, CharSequence limitSql,
			List<Object> parameters);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, CharSequence limitSql,
			List<Object> parameters, String tableAlias);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, List<String> additionalSelectColumnList,
			CharSequence orderBySql, CharSequence limitSql, int offset, int length, List<Object> parameters);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, List<String> additionalSelectColumnList,
			CharSequence orderBySql, CharSequence limitSql, int offset, int length, List<Object> parameters, String tableAlias);

	IResultSet createResultSet(String tableName, String idFieldName, Class<?> idFieldType, String fieldsSQL, String additionalWhereSQL, List<?> ids);
}
