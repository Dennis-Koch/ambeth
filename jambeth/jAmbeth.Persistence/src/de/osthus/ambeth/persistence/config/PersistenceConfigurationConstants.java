package de.osthus.ambeth.persistence.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;
import de.osthus.ambeth.ioc.PersistenceModule;

@ConfigurationConstants
public final class PersistenceConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String BatchSize = "persistence.ids.batchsize";

	@ConfigurationConstantDescription("TODO")
	public static final String PreparedBatchSize = "persistence.ids.preparedbatchsize";

	@ConfigurationConstantDescription("TODO")
	public static final String MaxInClauseBatchThreshold = "persistence.query.max_in_clause_batch_threshold";

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
	public static final String DatabaseFieldPrefix = "database.field.prefix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseFieldPostfix = "database.field.postfix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseSequencePrefix = "database.sequence.prefix";

	@ConfigurationConstantDescription("TODO")
	public static final String DatabaseSequencePostfix = "database.sequence.postfix";

	@ConfigurationConstantDescription("TODO")
	public static final String QueryCacheActive = "cache.query.active";

	@ConfigurationConstantDescription("True if the parallel read feature should be activated. If activated reads caused by read-only transactions will be executed in parallel if technically possible. Reads which are done after an uncommitted transaction has merged any changes will NOT be parallelized to ensure data consistency. It practice this feature is specifically worthy on find/retrieve service executions. In other cases it will intentionally not work - but will neither produce any overhead")
	public static final String ParallelReadActive = "persistence.parallel.read.active";

	@ConfigurationConstantDescription("Allows to define a custom executor bean to be used by the parallel read feature. This property is ignored if property 'ParallelReadActive' is not active. It defaults to '"
			+ PersistenceModule.DEFAULT_PARALLEL_READ_EXECUTOR_NAME + "'")
	public static final String ParallelReadExecutorName = "persistence.parallel.executor.name";

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
