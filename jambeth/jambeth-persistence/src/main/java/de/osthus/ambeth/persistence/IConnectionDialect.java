package de.osthus.ambeth.persistence;

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

	void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing);

	IList<IMap<String, String>> getExportedKeys(Connection connection, String schemaName) throws SQLException;

	ILinkedMap<String, IList<String>> getFulltextIndexes(Connection connection, String schemaName) throws SQLException;

	boolean isSystemTable(String tableName);

	boolean handleField(Class<?> fieldType, Object value, StringBuilder targetSb) throws Throwable;

	IList<String[]> disableConstraints(Connection connection);

	void enableConstraints(Connection connection, IList<String[]> disabled);

	void commit(Connection connection) throws SQLException;

	void rollback(Connection connection) throws SQLException;

	void releaseSavepoint(Savepoint savepoint, Connection connection) throws SQLException;

	int getOptimisticLockErrorCode();

	int getResourceBusyErrorCode();

	PersistenceException createPersistenceException(SQLException e, String relatedSql);

	boolean useVersionOnOptimisticUpdate();

	boolean isEmptySchema(Connection connection) throws SQLException;

	ResultSet getIndexInfo(Connection connection, String schemaName, String tableName, boolean unique) throws SQLException;

	Class<?> getComponentTypeByFieldTypeName(String additionalFieldInfo);

	String getFieldTypeNameByComponentType(Class<?> componentType);

	List<String> getTablesWithoutOptimisticLockTrigger(Connection connection) throws SQLException;

	String prepareCommand(String sqlCommand);

	String createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException;

	List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames) throws SQLException;

	List<String> buildDropAllSchemaContent(Connection conn, String schemaName);
}
