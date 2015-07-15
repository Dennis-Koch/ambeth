package de.osthus.ambeth.persistence.jdbc.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class PersistenceJdbcConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String AdditionalConnectionModules = "jdbc.connection.modules";

	/**
	 * Whether the recyclebin contents should be removed during startup in databases which support them (e.g. Oracle 10g/11g).Valid values are "true" and
	 * "false", default is "false".
	 */
	public static final String DatabaseAutoCleanupRecycleBin = "database.autocleanup.recyclebin";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseChangeNotificationActive = "jdbc.dcn.active";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseDumpSchema = "database.dump.schema";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseDumpRecords = "database.dump.records";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseDumpSchemaDirectory = "database.dump.schema.dir";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseDumpRecordsDirectory = "database.dump.records.dir";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseSchemaFile = "database.schema.file";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseSchemaCacheActive = "database.schema.cache.active";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseLocalFile = "database.local.file";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseLocalSchemaFile = "database.local.schema";

	/**
	 * The URL to the database connection, e.g. "jdbc:oracle:thin:@127.0.0.1:1521:SID".
	 */
	public static final String DatabaseConnection = "database.connection";

	/**
	 * The host of the database. Only evaluated if database.connection is not set.
	 */
	public static final String DatabaseHost = "database.host";

	/**
	 * The port of the database. Only evaluated if database.connection is not set.
	 */
	public static final String DatabasePort = "database.port";

	/**
	 * The protocol of the database. Only evaluated if database.connection is not set.
	 */
	public static final String DatabaseProtocol = "database.protocol";

	/**
	 * The name of the database. Only evaluated if database.connection is not set.
	 */
	public static final String DatabaseName = "database.name";

	/**
	 * The name of the schema database schema to use. If multiple schemas can be addressed all schema names have to be separated by semicolons.
	 */
	public static final String DatabaseSchemaName = "database.schema.name";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseInit = "database.init";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseUser = "database.user";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePass = "database.pass";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseRetryConnect = "database.retryconnect";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseRetrySleep = "database.retrysleep";

	@ConfigurationConstantDescription("TODO")
	public static final String JdbcTraceActive = "jdbc.trace.active";

	@ConfigurationConstantDescription("TODO")
	public static final String JdbcLogExceptionActive = "jdbc.logexception.active";

	/**
	 * Whether ambeth should use an integrated connection factory or connections are provided by data sources. If true, Ambeth will create its own database
	 * connection, otherwise they are managed by the data source. Valid values are "true" and "false", default is "true".
	 */
	public static final String IntegratedConnectionFactory = "database.connection.factory";

	@ConfigurationConstantDescription("TODO")
	public static final String IntegratedConnectionPool = "database.connection.pool";

	/**
	 * Whether to use strict behavior for database connections. Valid values are "true" and "false", default is "false".
	 */
	public static final String DatabaseBehaviourStrict = "database.behaviour.strict";

	/**
	 * The name of the datasource to use.
	 */
	public static final String DataSourceName = "datasource.name";

	/**
	 * Reference to prepared connections to the persistence store, which can be used instead of creating new ones. TODO: how can they be referenced in a text
	 * property?
	 */
	public static final String PreparedConnectionInstances = "connections.prepared";

	private PersistenceJdbcConfigurationConstants()
	{
		// Intended blank
	}
}
