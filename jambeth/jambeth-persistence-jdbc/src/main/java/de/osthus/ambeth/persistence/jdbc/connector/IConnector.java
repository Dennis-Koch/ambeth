package de.osthus.ambeth.persistence.jdbc.connector;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public interface IConnector
{
	void handleProperties(Properties props, String databaseProtocol);

	void handleProd(IBeanContextFactory beanContextFactory, String databaseProtocol);
}
