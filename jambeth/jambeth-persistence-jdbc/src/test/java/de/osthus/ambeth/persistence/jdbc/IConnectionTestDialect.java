package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import de.osthus.ambeth.config.IProperties;

public interface IConnectionTestDialect
{
	List<String> buildDropAllSchemaContent(Connection conn, String schemaName);

	String createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException;

	boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps) throws SQLException;

	List<String> getTablesWithoutOptimisticLockTrigger(Connection connection) throws SQLException;

	boolean isEmptySchema(Connection connection) throws SQLException;

	String prepareCommand(String sqlCommand);

	void preProcessConnectionForTest(Connection connection, String[] schemaNames, boolean forcePreProcessing);

	void preStructureRebuild(Connection connection) throws SQLException;
}
