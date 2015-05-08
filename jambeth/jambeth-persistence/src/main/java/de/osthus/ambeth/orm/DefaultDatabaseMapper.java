package de.osthus.ambeth.orm;

import java.sql.Connection;

import de.osthus.ambeth.database.IDatabaseMapper;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.util.ParamChecker;

public class DefaultDatabaseMapper implements IDatabaseMapper, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String idName = "Id";

	protected String versionName = "Version";

	@Autowired
	protected IOrmPatternMatcher ormPatternMatcher;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(idName, "idName");
		ParamChecker.assertNotNull(versionName, "versionName");
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
	public void mapFields(Connection connection, String[] schemaNames, IDatabaseMetaData database)
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
	public void mapLinks(Connection connection, String[] schemaNames, IDatabaseMetaData database)
	{
		// Intended blank
	}

	/**
	 * Creates the mapping between ID and version fields and members.
	 * 
	 * @param table
	 *            Table to map to.
	 */
	protected void mapIdAndVersion(ITableMetaData table)
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
	protected void mapIdAndVersion(ITableMetaData table, String idName, String versionName)
	{
		IFieldMetaData[] idFields = table.getIdFields();
		String[] idNames = idName.split("-");
		if (idNames.length != idFields.length)
		{
			throw new IllegalArgumentException("Member count (" + idNames.length + ") does not match with field count (" + idFields.length + ")");
		}
		for (int a = idFields.length; a-- > 0;)
		{
			table.mapField(idFields[a].getName(), idNames[a]);
		}
		IFieldMetaData versionField = table.getVersionField();
		if (versionField != null)
		{
			table.mapField(versionField.getName(), versionName);
		}
	}
}
