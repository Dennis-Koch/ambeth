package com.koch.ambeth.persistence.jdbc.testconnector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.persistence.jdbc.testconnector.ITestConnector;
import com.koch.ambeth.persistence.pg.PostgresConnectionUrlProvider;
import com.koch.ambeth.persistence.pg.PostgresDialect;
import com.koch.ambeth.persistence.pg.PostgresTestDialect;
import com.koch.ambeth.persistence.pg.PostgresTestModule;

public class JDBC_POSTGRESQL implements ITestConnector
{
	@Override
	public void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(PostgresConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(PostgresDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(PostgresTestDialect.class).autowireable(IConnectionTestDialect.class);
	}

	@Override
	public void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(PostgresTestModule.class);
	}
}
