package de.osthus.ambeth.persistence.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

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

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePoolMaxUsed = "database.pool.maxused";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePoolMaxUnused = "database.pool.maxunused";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePoolPassivate = "database.pool.passivate.active";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePoolTryCount = "database.pool.maxtry";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePoolTryTimeSpan = "database.pool.maxwait";

	@ConfigurationConstantDescription("Timeout before completely giving up to get a database connection")
	public static final String DatabasePoolTryTime = "database.pool.maxtrytime";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseTableIgnore = "database.table.ignore";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseTablePrefix = "database.table.prefix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseTablePostfix = "database.table.postfix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseArchiveTablePrefix = "database.archivetable.prefix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseArchiveTablePostfix = "database.archivetable.postfix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePermissionGroupPrefix = "database.permissiongrouptable.prefix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabasePermissionGroupPostfix = "database.permissiongrouptable.postfix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseFieldPrefix = "database.field.prefix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseFieldPostfix = "database.field.postfix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseSequencePrefix = "database.sequence.prefix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseSequencePostfix = "database.sequence.postfix";

	@ConfigurationConstantDescription("TODO")
	public static final String QueryCacheActive = "cache.query.active";

	@ConfigurationConstantDescription("TODO")
	public static final String LinkClass = "link.type";

	@ConfigurationConstantDescription("TODO")
	public static final String AutoArrayTypes = "database.auto.arraytypes";

	@ConfigurationConstantDescription("TODO")
	public static final String AutoIndexForeignKeys = "database.auto.indexonfk";

	@ConfigurationConstantDescription("TODO")
	public static final String ExternalTransactionManager = "database.transaction.external";

	private PersistenceConfigurationConstants()
	{
		// Intended blank
	}
}
