package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc-test
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.koch.ambeth.util.config.IProperties;

public interface IConnectionTestDialect {
	void resetStatementCache(Connection connection);

	void dropAllSchemaContent(Connection conn, String schemaName);

	List<String> getTablesWithoutOptimisticLockTrigger(Connection connection) throws SQLException;

	String[] createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException;

	String[] createAdditionalTriggers(Connection connection, String tableName) throws SQLException;

	List<String> getTablesWithoutPermissionGroup(Connection conn) throws SQLException;

	String[] createPermissionGroup(Connection conn, String tableName) throws SQLException;

	boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword,
			IProperties testProps) throws SQLException;

	void dropCreatedTestUser(String userName, String userPassword, IProperties testProps)
			throws SQLException;

	boolean isEmptySchema(Connection connection) throws SQLException;

	void preProcessConnectionForTest(Connection connection, String[] schemaNames,
			boolean forcePreProcessing);

	void preStructureRebuild(Connection connection) throws SQLException;
}
