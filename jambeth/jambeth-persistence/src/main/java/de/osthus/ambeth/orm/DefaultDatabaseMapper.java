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
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.util.ParamChecker;

public class DefaultDatabaseMapper implements IDatabaseMapper, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(DefaultDatabaseMapper.class)
	private ILogger log;

	protected ITypeInfoProvider typeInfoProvider;

	protected String idName = "Id";

	protected String versionName = "Version";

	protected String tablePrefix = "";

	protected String tablePostfix = "";

	protected String archiveTablePrefix = "";

	protected String archiveTablePostfix = "";

	protected String fieldPrefix = "";

	protected String fieldPostfix = "";

	protected String sequencePrefix = "";

	protected String sequencePostfix = "";

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(typeInfoProvider, "typeInfoProvider");
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

	public void setTypeInfoProvider(ITypeInfoProvider typeInfoProvider)
	{
		this.typeInfoProvider = typeInfoProvider;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseTablePrefix, mandatory = false)
	public void setTablePrefix(String prefix)
	{
		this.tablePrefix = prefix;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseTablePostfix, mandatory = false)
	public void setTablePostfix(String postfix)
	{
		this.tablePostfix = postfix;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseArchiveTablePrefix, mandatory = false)
	public void setArchiveTablePrefix(String prefix)
	{
		this.archiveTablePrefix = prefix;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseArchiveTablePostfix, mandatory = false)
	public void setArchiveTablePostfix(String postfix)
	{
		this.archiveTablePostfix = postfix;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, mandatory = false)
	public void setFieldPrefix(String prefix)
	{
		this.fieldPrefix = prefix;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseFieldPostfix, mandatory = false)
	public void setFieldPostfix(String postfix)
	{
		this.fieldPostfix = postfix;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseSequencePrefix, mandatory = false)
	public void setSequencePrefix(String prefix)
	{
		this.sequencePrefix = prefix;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseSequencePostfix, mandatory = false)
	public void setSequencePostfix(String postfix)
	{
		this.sequencePostfix = postfix;
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
