package com.koch.ambeth.persistence.config;

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class PersistenceConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String BatchSize = "persistence.ids.batchsize";

	@ConfigurationConstantDescription("TODO")
	public static final String PreparedBatchSize = "persistence.ids.preparedbatchsize";

	@ConfigurationConstantDescription("TODO")
	public static final String SequencePrefetchSize = "persistence.ids.sequence.prefetchsize";

	@ConfigurationConstantDescription("TODO")
	public static final String FetchSize = "persistence.fetchsize";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePoolMaxPending = "database.pool.maxpending";

	/**
	 * Maximum number of used connections in the database pool. If the limit is exceeded all further requests for connections are paused until either a
	 * connection is available or the timeout from {@link #DatabasePoolTryTimeSpan} is reached. Valid values are all numbers > 0, default is 2.
	 * 
	 */
	public static final String DatabasePoolMaxUsed = "database.pool.maxused";

	/**
	 * Maximum number of unused connections in the connection pool. If the limit is exceeded all further connections which are not used anymore are closed and
	 * removed from the pool. Valid values are all numbers > 0, default is 2.
	 */
	public static final String DatabasePoolMaxUnused = "database.pool.maxunused";

	/**
	 * Ensures that the database connection gets passivated when not used. Valid values are "true" and "false", default is "false".
	 */
	public static final String DatabasePoolPassivate = "database.pool.passivate.active";

	/**
	 * Number of attempts ambeth should try to acquire a database instance. If the number of attempts exceeds the limit an IllegalStateException will be thrown.
	 * If the value is set to 0 Ambeth will try to get a connection until it successful retrieves one. Valid values are all numbers starting from 0, default is
	 * 1.
	 */
	public static final String DatabasePoolTryCount = "database.pool.maxtry";

	/**
	 * Maximum timespan in ms in which a database connection should be received. Valid values are numbers > 0, default is 30000.
	 */
	public static final String DatabasePoolTryTimeSpan = "database.pool.maxwait";

	/**
	 * Timeout in ms before completely giving up to get a database connection. Valid values are number > 0, default is 30000.
	 */
	public static final String DatabasePoolTryTime = "database.pool.maxtrytime";

	/**
	 * A list of tables to ignore during database scanning. By default Ambeth tries to recognize and analyze all tables. If some tables should not be scanned or
	 * analyzed they have to be in this list. Table names have to be separated by semicolon ";" or colon ":".
	 */
	public static final String DatabaseTableIgnore = "database.table.ignore";

	/**
	 * The prefix to the name of every table representing an entity has to start with. Valid values are all strings of chars which can be used in database table
	 * names, default value is empty.
	 */
	public static final String DatabaseTablePrefix = "database.table.prefix";

	/**
	 * The postfix to the name of every table representing an entity has to start with. Valid values are all strings of chars which can be used in database
	 * table names, default value is empty.
	 */
	public static final String DatabaseTablePostfix = "database.table.postfix";

	/**
	 * The prefix the name of a table used to archive objects has to start with. Valid values are all strings of chars which can be used in database table
	 * names, default value is empty.
	 */
	public static final String DatabaseArchiveTablePrefix = "database.archivetable.prefix";

	/**
	 * The postfix the name of a table used to archive objects has to end with. Valid values are all strings of chars which can be used in database table names,
	 * default value is empty. If neither postfix nor prefix ({@link database.archivetable.prefix}) is given the postfix is set to "_ARC".
	 */
	public static final String DatabaseArchiveTablePostfix = "database.archivetable.postfix";

	/**
	 * The prefix for a database table to be recognized as a permission group table for another table. E.g. the prefix is "PERMISSIONGROUP_" and the table the
	 * permissions should be applied to is named "ADDRESSES", the table "PERMISSIONGROUP_ADDRESSES" would be recognized as a table containing information about
	 * the permissions to access table "ADDRESSES".
	 */
	public static final String DatabasePermissionGroupPrefix = "database.permissiongrouptable.prefix";

	/**
	 * The postfix for a database table to be recognized as a permission group table for another table. E.g. the postfix is "_PG" and the table the permissions
	 * should be applied to is named "ADDRESSES", the table "ADDRESSES_PG" would be recognized as a table containing information about the permissions to access
	 * table "ADDRESSES".
	 */
	public static final String DatabasePermissionGroupPostfix = "database.permissiongrouptable.postfix";

	/**
	 * The prefix for a database field to be recognized as the corresponding database part for a property in an entity. E.g. the prefix is "PREFIX" and the java
	 * property is "field1" the database field "PREFIX_FIELD1" would automatically be recognized as the counterpart to the java property.
	 */
	public static final String DatabaseFieldPrefix = "database.field.prefix";

	/**
	 * The postfix for a database field to be recognized as the corresponding database part for a property in an entity. E.g. the postfix is "_POSTFIX" and the
	 * java property is "field1" the database field "FIELD1_POSTFIX" would automatically be recognized as the counterpart to the java property.
	 */
	public static final String DatabaseFieldPostfix = "database.field.postfix";

	/**
	 * The prefix every sequence name has to end with. Valid values are all strings of chars which can be used in database table names, default value is empty.
	 */
	public static final String DatabaseSequencePrefix = "database.sequence.prefix";

	/**
	 * The postfix every sequence name has to end with. Valid values are all strings of chars which can be used in database table names, default value is empty.
	 */
	public static final String DatabaseSequencePostfix = "database.sequence.postfix";

	/**
	 * Defines whether the query cache should be active, which stores results of queries to the persistence layer. Valid values are "true" and "false", default
	 * is "true".
	 */
	public static final String QueryCacheActive = "cache.query.active";

	@ConfigurationConstantDescription("TODO")
	public static final String LinkClass = "link.type";

	/**
	 * Defines whether ambeth should create array types (e.g. STRING_ARRAY, LONG_ARRAY) in the database if they do not exist yet. Valid value are "true" and
	 * "false", default is "true".
	 */
	public static final String AutoArrayTypes = "database.auto.arraytypes";

	/**
	 * Defines whether ambeth should create indexes for foreign key columns automatically if they do not exist yet. Valid value are "true" and "false", default
	 * is "false".
	 */
	public static final String AutoIndexForeignKeys = "database.auto.indexonfk";

	/**
	 * Defines whether the transaction is managed external ("true") or Ambeth should handle it ("false"). If the transaction is managed externally Ambeth does
	 * not commit or rollback any transaction and expects the manager to do the corresponding action. Valid values are "true" and "false", default is "false".
	 */
	public static final String ExternalTransactionManager = "database.transaction.external";

	private PersistenceConfigurationConstants()
	{
		// Intended blank
	}
}
