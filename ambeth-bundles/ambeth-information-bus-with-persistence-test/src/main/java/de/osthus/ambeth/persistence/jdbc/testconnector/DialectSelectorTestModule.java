package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public class DialectSelectorTestModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol)
	protected String databaseProtocol;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		ITestConnector connector = DialectSelectorSchemaModule.loadTestConnector(databaseProtocol);
		connector.handleTest(beanContextFactory, databaseProtocol);
	}
}
