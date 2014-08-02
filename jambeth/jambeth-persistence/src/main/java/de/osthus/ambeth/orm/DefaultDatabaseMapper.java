package de.osthus.ambeth.orm;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.IDatabaseMapper;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.util.ParamChecker;

public class DefaultDatabaseMapper implements IDatabaseMapper, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String idName = "Id";

	protected String versionName = "Version";

	@Property(name = PersistenceConfigurationConstants.DatabaseTablePrefix, defaultValue = "")
	protected String tablePrefix;

	@Property(name = PersistenceConfigurationConstants.DatabaseTablePostfix, defaultValue = "")
	protected String tablePostfix;

	@Property(name = PersistenceConfigurationConstants.DatabaseArchiveTablePrefix, defaultValue = "")
	protected String archiveTablePrefix;

	@Property(name = PersistenceConfigurationConstants.DatabaseArchiveTablePostfix, defaultValue = "")
	protected String archiveTablePostfix;

	@Property(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, defaultValue = "")
	protected String fieldPrefix;

	@Property(name = PersistenceConfigurationConstants.DatabaseFieldPostfix, defaultValue = "")
	protected String fieldPostfix;

	@Property(name = PersistenceConfigurationConstants.DatabaseSequencePrefix, defaultValue = "")
	protected String sequencePrefix;

	@Property(name = PersistenceConfigurationConstants.DatabaseSequencePostfix, defaultValue = "")
	protected String sequencePostfix = "";

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(idName, "idName");
		ParamChecker.assertNotNull(versionName, "versionName");

		if (this.archiveTablePrefix.isEmpty() && this.archiveTablePostfix.isEmpty())
		{
			this.archiveTablePostfix = "_ARC";
		}
		if (this.sequencePrefix.isEmpty() && this.sequencePostfix.isEmpty())
		{
			this.sequencePostfix = "_SEQ";
		}
	}

	/**
	 * Setter for the name of the ID member. (default = "Id")
	 * 
	 * @param idName
	 *            New name of the ID member.
	 */
	public void setIdName(String idName)
	{
		this.idName = idName;
	}

	/**
	 * Setter for the name of the version member. (default = "Version")
	 * 
	 * @param versionName
	 *            New name of the version member.
	 */
	public void setVersionName(String versionName)
	{
		this.versionName = versionName;
	}

	/**
	 * Method to implement in child class to setup the mappings between a database table and an entity type and between the database fields and the entity
	 * members. Including the ID and version fields.
	 * 
	 * Method to implement in child class to setup the mappings between a database table and an entity type and between the database fields and the entity
	 * members. Including the ID and version fields.
	 * 
	 * @param database
	 *            Database to map to.
	 */
	@Override
	public void mapFields(IDatabase database)
	{
		// Intended blank
	}

	/**
	 * TODO JavaDoc comment.
	 * 
	 * @param database
	 *            Database to map to.
	 */
	@Override
	public void mapLinks(IDatabase database)
	{
		// Intended blank
	}

	/**
	 * Creates the mapping between ID and version fields and members.
	 * 
	 * @param table
	 *            Table to map to.
	 */
	protected void mapIdAndVersion(ITable table)
	{
		mapIdAndVersion(table, idName, versionName);
	}

	/**
	 * Creates the mapping between ID and version fields and members.
	 * 
	 * @param table
	 *            Table to map to.
	 * @param idName
	 *            Name of the id field.
	 * @param versionName
	 *            Name of the version field.
	 */
	protected void mapIdAndVersion(ITable table, String idName, String versionName)
	{
		table.mapField(table.getIdField().getName(), idName);
		IField versionField = table.getVersionField();
		if (versionField != null)
		{
			table.mapField(versionField.getName(), versionName);
		}
	}
}
