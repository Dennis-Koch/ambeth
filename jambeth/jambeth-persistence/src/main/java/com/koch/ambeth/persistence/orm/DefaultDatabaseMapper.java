package com.koch.ambeth.persistence.orm;

/*-
 * #%L
 * jambeth-persistence
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

import java.sql.Connection;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.database.IDatabaseMapper;
import com.koch.ambeth.util.ParamChecker;

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
