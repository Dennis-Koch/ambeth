package de.osthus.ambeth.persistence;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;

import javax.persistence.PersistenceException;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;

public interface IConnectionDialect
{
	Blob createBlob(Connection connection) throws SQLException;

	Clob createClob(Connection connection) throws SQLException;

	String toDefaultCase(String unquotedIdentifier);

	void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing);

	IList<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames) throws SQLException;

	ILinkedMap<String, IList<String>> getFulltextIndexes(Connection connection, String schemaName) throws SQLException;

	boolean isSystemTable(String tableName);

	IList<String> disableConstraints(Connection connection, String... schemaNames);

	void enableConstraints(Connection connection, IList<String> disabled);

	void commit(Connection connection) throws SQLException;

	void rollback(Connection connection) throws SQLException;

	void releaseSavepoint(Savepoint savepoint, Connection connection) throws SQLException;

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

	int getMaxInClauseBatchThreshold();

	boolean isEmptyStringAsNullStored(IFieldMetaData field);

	Object convertToFieldType(IFieldMetaData field, Object value);

	Object convertFromFieldType(IDatabase database, IFieldMetaData field, Class<?> expectedType, Object value);
}
