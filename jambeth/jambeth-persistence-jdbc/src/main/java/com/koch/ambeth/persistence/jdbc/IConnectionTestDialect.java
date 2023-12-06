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

import com.koch.ambeth.util.config.IProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IConnectionTestDialect {
    void resetStatementCache(Connection connection);

    void dropAllSchemaContent(Connection conn, String schemaName);

    List<String> getTablesWithoutOptimisticLockTrigger(Connection connection);

    String[] createOptimisticLockTrigger(Connection connection, String tableName);

    String[] createAdditionalTriggers(Connection connection, String tableName);

    List<String> getTablesWithoutPermissionGroup(Connection conn);

    String[] createPermissionGroup(Connection conn, String tableName);

    boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps);

    void dropCreatedTestUser(String userName, String userPassword, IProperties testProps) throws SQLException;

    boolean isEmptySchema(Connection connection);

    void preProcessConnectionForTest(Connection connection, String[] schemaNames, boolean forcePreProcessing);

    void preStructureRebuild(Connection connection);

    void flushSharedObjects(Connection connection);
}
