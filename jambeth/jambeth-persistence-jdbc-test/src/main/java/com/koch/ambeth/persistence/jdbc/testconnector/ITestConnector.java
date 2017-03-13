package com.koch.ambeth.persistence.jdbc.testconnector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public interface ITestConnector
{
	void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol);

	void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol);
}
