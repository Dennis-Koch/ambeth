package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public interface ITestConnector
{
	void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol);

	void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol);
}
