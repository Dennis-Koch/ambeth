package com.koch.ambeth.persistence.jdbc.connection;

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
