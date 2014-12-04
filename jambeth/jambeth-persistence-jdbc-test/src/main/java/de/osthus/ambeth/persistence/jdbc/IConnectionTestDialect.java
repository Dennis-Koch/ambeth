package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import de.osthus.ambeth.config.IProperties;

public interface IConnectionTestDialect
{
	void dropAllSchemaContent(Connection conn, String schemaName);

	List<String> getTablesWithoutOptimisticLockTrigger(Connection connection) throws SQLException;

	String[] createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException;

	List<String> getTablesWithoutPermissionGroup(Connection conn) throws SQLException;

	String[] createPermissionGroup(Connection conn, String tableName) throws SQLException;

	boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps) throws SQLException;

	boolean isEmptySchema(Connection connection) throws SQLException;

	String prepareCommand(String sqlCommand);

	void preProcessConnectionForTest(Connection connection, String[] schemaNames, boolean forcePreProcessing);

	void preStructureRebuild(Connection connection) throws SQLException;
}
