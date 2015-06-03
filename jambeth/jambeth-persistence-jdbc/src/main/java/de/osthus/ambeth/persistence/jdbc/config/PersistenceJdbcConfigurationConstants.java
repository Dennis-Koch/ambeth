package de.osthus.ambeth.persistence.jdbc.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class PersistenceJdbcConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String AdditionalConnectionModules = "jdbc.connection.modules";

	@ConfigurationConstantDescription("TODO")
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

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseChecksumAlgorithm = "database.checksum.algorithm";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseConnection = "database.connection";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseHost = "database.host";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePort = "database.port";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseProtocol = "database.protocol";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseName = "database.name";

	@ConfigurationConstantDescription("TODO")
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

	@ConfigurationConstantDescription("TODO")
	public static final String IntegratedConnectionFactory = "database.connection.factory";

	@ConfigurationConstantDescription("TODO")
	public static final String IntegratedConnectionPool = "database.connection.pool";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseBehaviourStrict = "database.behaviour.strict";

	@ConfigurationConstantDescription("TODO")
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
