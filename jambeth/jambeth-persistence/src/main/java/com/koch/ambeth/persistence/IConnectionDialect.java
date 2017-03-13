package com.koch.ambeth.persistence;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;

import javax.persistence.PersistenceException;

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.state.IStateRollback;

public interface IConnectionDialect
{
	String escapeName(CharSequence symbolName);

	IAppendable escapeName(CharSequence symbolName, IAppendable sb);

	String escapeSchemaAndSymbolName(CharSequence schemaName, CharSequence symbolName);

	String getEscapeLiteral();

	String getSelectForUpdateFragment();

	Blob createBlob(Connection connection) throws SQLException;

	Clob createClob(Connection connection) throws SQLException;

	String toDefaultCase(String unquotedIdentifier);

	void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing);

	IList<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames) throws SQLException;

	ILinkedMap<String, IList<String>> getFulltextIndexes(Connection connection, String schemaName) throws SQLException;

	boolean isSystemTable(String tableName);

	IStateRollback disableConstraints(Connection connection, String... schemaNames);

	void commit(Connection connection) throws SQLException;

	void rollback(Connection connection) throws SQLException;

	void releaseSavepoint(Savepoint savepoint, Connection connection) throws SQLException;

	IOperand getRegexpLikeFunction(IOperand sourceString, IOperand pattern, IOperand matchParameter);

	IOperand getLimitOperand(IOperand operand, IValueOperand valueOperand);

	int getResourceBusyErrorCode();

	PersistenceException createPersistenceException(SQLException e, String relatedSql);

	boolean useVersionOnOptimisticUpdate();

	ResultSet getIndexInfo(Connection connection, String schemaName, String tableName, boolean unique) throws SQLException;

	Class<?> getComponentTypeByFieldTypeName(String additionalFieldInfo);

	String getFieldTypeNameByComponentType(Class<?> componentType);

	List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) throws SQLException;

	List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames) throws SQLException;

	List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames) throws SQLException;

	IList<IColumnEntry> getAllFieldsOfTable(Connection connection, String fqTableName) throws SQLException;

	IList<String> queryDefault(Connection connection, String resultColumnName, String sql, Object... args) throws SQLException;

	int getMaxInClauseBatchThreshold();

	boolean isEmptyStringAsNullStored(IFieldMetaData field);

	Object convertToFieldType(IFieldMetaData field, Object value);

	Object convertFromFieldType(IDatabase database, IFieldMetaData field, Class<?> expectedType, Object value);

	boolean isTransactionNecessaryDuringLobStreaming();

	void appendIsInOperatorClause(IAppendable appendable);

	boolean isCompactMultiValueRecommended(IList<Object> values);

	void handleWithMultiValueLeftField(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters, IList<IList<Object>> splitValues,
			boolean caseSensitive, Class<?> leftOperandFieldType);

	/**
	 * append the best suited list clause IN( ?,?,?,?,?) (with ids as single parameters) _OR_ = ANY [?] (with an array)
	 * 
	 * @param parameters
	 * @param sb
	 * @param fieldType
	 * @param splittedIds
	 */
	void appendListClause(List<Object> parameters, IAppendable sb, Class<?> fieldType, IList<Object> splittedIds);

	/**
	 * Replace Oracle SQL with connection dialect specific SQL
	 * 
	 * @param sqlCommand
	 * @return
	 */
	String prepareCommand(String sqlCommand);

	SelectPosition getLimitPosition();

	int getColumnCountForLinkTable();

	String buildClearTableSQL(String tableName);
}
