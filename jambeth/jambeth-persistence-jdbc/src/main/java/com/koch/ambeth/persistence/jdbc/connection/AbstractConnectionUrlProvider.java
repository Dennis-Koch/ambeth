package com.koch.ambeth.persistence.jdbc.connection;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.config.IProperties;

public abstract class AbstractConnectionUrlProvider implements IDatabaseConnectionUrlProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IProperties properties;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseConnection, mandatory = false)
	protected String databaseConnection;

	protected String resolveProperty(String propertyName)
	{
		String value = properties.getString(propertyName);
		if (value == null)
		{
			throw new IllegalStateException("Property could not be resolved: " + propertyName);
		}
		return value;
	}

	@Override
	public String getConnectionUrl()
	{
		if (databaseConnection != null)
		{
			return databaseConnection;
		}
		return getConnectionUrlIntern();
	}

	protected abstract String getConnectionUrlIntern();
}
