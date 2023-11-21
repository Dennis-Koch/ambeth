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
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import lombok.SneakyThrows;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseProtocolResolver {

	private static final Pattern ORACLE_PATTERN = Pattern.compile("^(jdbc:[^:]+:[^:]+)(?::[^:/]+/[^:]+)?:@.*");
	private static final Pattern NON_ORACLE_PATTERN = Pattern.compile("^(jdbc:[^:]+)(:.*)?");

	@SneakyThrows
	public static void enrichWithDatabaseProtocol(Properties contextProperties) {
		var databaseProtocol = contextProperties.getString(PersistenceJdbcConfigurationConstants.DatabaseProtocol);
		if (databaseProtocol != null) {
			return;
		}
		try {
			var connectionUrl = resolveConnectionUrl(contextProperties);
			Matcher urlMatcher;
			if (connectionUrl.contains(":@")) {
				// Oracle
				// jdbc:oracle:driver:username/password@host:port:database
				urlMatcher = ORACLE_PATTERN.matcher(connectionUrl);
				// Ignore ([^:]+)(?::(\\d++))?(?::([^:]+))?$ => host:post/database?params
			}
			else {
				// Use everything from jdbc to the second :
				// Postgresql, MySql, SqlServer
				// jdbc:driver://host:port/database?user=...
				// jdbc:h2:tcp://localhost/~/test;AUTO_RECONNECT=TRUE
				// Derby, DB2, Sybase, H2 non-urls
				// jdbc:driver:...
				urlMatcher = NON_ORACLE_PATTERN.matcher(connectionUrl);
			}
			if (contextProperties.get(PersistenceJdbcConfigurationConstants.DatabaseConnection) == null) {
				contextProperties.putString(PersistenceJdbcConfigurationConstants.DatabaseConnection, connectionUrl);
			}
			if (urlMatcher.matches()) {
				var protocol = urlMatcher.group(1);
				contextProperties.putString(PersistenceJdbcConfigurationConstants.DatabaseProtocol,
						protocol);
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, DatabaseProtocolResolver.class.getSimpleName()
					+ " was not able to get the database protocol from the dataSource");
			// Do nothing and hope that the connection is configured elsewhere
		}
	}

	@SneakyThrows
	private static String resolveConnectionUrl(Properties contextProperties) {
		var useIntegrationConnectionFactory = Boolean.parseBoolean(contextProperties.getString(PersistenceJdbcConfigurationConstants.IntegratedConnectionFactory, "true"));
		if (useIntegrationConnectionFactory) {
			var connectionUrl = contextProperties.getString(PersistenceJdbcConfigurationConstants.DatabaseConnection);
			if (connectionUrl == null) {
				throw new IllegalStateException("No connection URL resolvable. Maybe you need to specify '" + PersistenceJdbcConfigurationConstants.DatabaseConnection + "'");
			}
			return connectionUrl;
		}
		DataSource dataSource = contextProperties.get(PersistenceJdbcConfigurationConstants.DataSourceInstance);
		if (dataSource == null) {
			String dataSourceName = contextProperties.get(PersistenceJdbcConfigurationConstants.DataSourceName);
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
		throw new IllegalStateException("No connection URL resolvable. As '" + PersistenceJdbcConfigurationConstants.DatabaseConnection + "'=false you may need to specify '" + PersistenceJdbcConfigurationConstants.DataSourceInstance + "'");
	}
}
