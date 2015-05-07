package de.osthus.ambeth.persistence.jdbc.connection;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

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
