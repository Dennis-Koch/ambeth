package de.osthus.ambeth.testutil.persistencerunner;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.testutil.IPropertiesProvider;

public class AlternativeUserPropertiesProvider implements IPropertiesProvider
{
	@Override
	public void fillProperties(Properties props)
	{
		String existingUser = props.getString(PersistenceJdbcConfigurationConstants.DatabaseUser);
		props.putString(PersistenceJdbcConfigurationConstants.DatabaseUser, existingUser + "_2nd");
	}
}