package com.koch.ambeth.persistence.jdbc.connector;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import lombok.SneakyThrows;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseProtocolResolver {

    private static final Pattern ORACLE_PATTERN = Pattern.compile("^(jdbc:[^:]+:[^:]+)(?::[^:/]+/[^:]+)?:@.*");
    private static final Pattern NON_ORACLE_PATTERN = Pattern.compile("^(jdbc:[^:]+)(:.*)?");

    public static void enrichWithDatabaseProtocol(java.util.Properties props) {
        enrichWithDatabaseProtocol(props::get, props::put);
    }

    public static void enrichWithDatabaseProtocol(Properties props) {
        enrichWithDatabaseProtocol(props::get, props::put);
    }

    @SneakyThrows
    public static void enrichWithDatabaseProtocol(Function<String, Object> propGetter, BiConsumer<String, String> propSetter) {
        var databaseProtocol = propGetter.apply(PersistenceJdbcConfigurationConstants.DatabaseProtocol);
        if (databaseProtocol != null) {
            return;
        }
        try {
            var connectionUrl = resolveConnectionUrl(propGetter);
            Matcher urlMatcher;
            if (connectionUrl.contains(":@")) {
                // Oracle
                // jdbc:oracle:driver:username/password@host:port:database
                urlMatcher = ORACLE_PATTERN.matcher(connectionUrl);
                // Ignore ([^:]+)(?::(\\d++))?(?::([^:]+))?$ => host:post/database?params
            } else {
                // Use everything from jdbc to the second :
                // Postgresql, MySql, SqlServer
                // jdbc:driver://host:port/database?user=...
                // jdbc:h2:tcp://localhost/~/test;AUTO_RECONNECT=TRUE
                // Derby, DB2, Sybase, H2 non-urls
                // jdbc:driver:...
                urlMatcher = NON_ORACLE_PATTERN.matcher(connectionUrl);
            }
            if (propGetter.apply(PersistenceJdbcConfigurationConstants.DatabaseConnection) == null) {
                propSetter.accept(PersistenceJdbcConfigurationConstants.DatabaseConnection, connectionUrl);
            }
            if (urlMatcher.matches()) {
                var protocol = urlMatcher.group(1);
                propSetter.accept(PersistenceJdbcConfigurationConstants.DatabaseProtocol, protocol);
            }
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, DatabaseProtocolResolver.class.getSimpleName() + " was not able to get the database protocol from the dataSource");
            // Do nothing and hope that the connection is configured elsewhere
        }
    }

    @SneakyThrows
    private static String resolveConnectionUrl(Function<String, Object> propGetter) {
        var integratedConnectionFactory = propGetter.apply(PersistenceJdbcConfigurationConstants.IntegratedConnectionFactory);
        if (integratedConnectionFactory == null) {
            integratedConnectionFactory = "true";
        }
        var useIntegrationConnectionFactory = Boolean.parseBoolean(integratedConnectionFactory != null ? integratedConnectionFactory.toString() : null);
        if (useIntegrationConnectionFactory) {
            var connectionUrl = (String) propGetter.apply(PersistenceJdbcConfigurationConstants.DatabaseConnection);
            if (connectionUrl == null) {
                throw new IllegalStateException("No connection URL resolvable. Maybe you need to specify '" + PersistenceJdbcConfigurationConstants.DatabaseConnection + "'");
            }
            return connectionUrl;
        }
        DataSource dataSource = (DataSource) propGetter.apply(PersistenceJdbcConfigurationConstants.DataSourceInstance);
        if (dataSource == null) {
            String dataSourceName = (String) propGetter.apply(PersistenceJdbcConfigurationConstants.DataSourceName);
            if (dataSourceName != null) {
                var ic = new InitialContext();
                dataSource = (DataSource) ic.lookup(dataSourceName);
            }
        }
        if (dataSource != null) {
            try (var connection = dataSource.getConnection()) {
                return connection.getMetaData().getURL();
            }
        }
        throw new IllegalStateException(
                "No connection URL resolvable. As '" + PersistenceJdbcConfigurationConstants.DatabaseConnection + "'=false you may need to specify '" + PersistenceJdbcConfigurationConstants.DataSourceInstance + "'");
    }
}
