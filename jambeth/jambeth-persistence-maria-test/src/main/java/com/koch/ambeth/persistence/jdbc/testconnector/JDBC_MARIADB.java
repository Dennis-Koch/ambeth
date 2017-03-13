package com.koch.ambeth.persistence.jdbc.testconnector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.persistence.jdbc.testconnector.ITestConnector;
import com.koch.ambeth.persistence.maria.MariaConnectionUrlProvider;
import com.koch.ambeth.persistence.maria.MariaDialect;
import com.koch.ambeth.persistence.maria.MariaTestDialect;
import com.koch.ambeth.persistence.maria.MariaTestModule;

public class JDBC_MARIADB implements ITestConnector
{
	@Override
	public void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(MariaConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(MariaDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(MariaTestDialect.class).autowireable(IConnectionTestDialect.class);
	}

	@Override
	public void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(MariaTestModule.class);
	}
}
