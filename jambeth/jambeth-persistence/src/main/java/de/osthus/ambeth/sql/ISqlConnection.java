package de.osthus.ambeth.sql;

import java.util.List;

import de.osthus.ambeth.collections.ILinkedMap;

public interface ISqlConnection
{
	void queueDelete(String tableName, String whereSql, ILinkedMap<Integer, Object> params);

	void queueDelete(String tableName, String[] whereSql);

	void queueDeleteAll(String tableName);

	void queueUpdate(String tableName, String valueAndNamesSql, String whereSql);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence whereSql, ILinkedMap<Integer, Object> params);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, ILinkedMap<Integer, Object> params);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, ILinkedMap<Integer, Object> params,
			String tableAlias);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, List<String> additionalSelectColumnList,
			CharSequence orderBySql, int offset, int length, ILinkedMap<Integer, Object> params);

	IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, List<String> additionalSelectColumnList,
			CharSequence orderBySql, int offset, int length, ILinkedMap<Integer, Object> params, String tableAlias);

	IResultSet createResultSet(String tableName, String idFieldName, Class<?> idFieldType, String fieldsSQL, String additionalWhereSQL, List<?> ids);
}
