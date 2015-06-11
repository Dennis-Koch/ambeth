package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import de.osthus.ambeth.pg.PostgresConnectionUrlProvider;
import de.osthus.ambeth.pg.PostgresDialect;
import de.osthus.ambeth.pg.PostgresTestDialect;
import de.osthus.ambeth.pg.PostgresTestModule;

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
