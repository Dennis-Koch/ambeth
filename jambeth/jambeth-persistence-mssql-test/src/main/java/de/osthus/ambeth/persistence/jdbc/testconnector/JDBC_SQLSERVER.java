package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.mssql.MSSqlConnectionUrlProvider;
import de.osthus.ambeth.mssql.MSSqlDialect;
import de.osthus.ambeth.mssql.MSSqlTestDialect;
import de.osthus.ambeth.mssql.MSSqlTestModule;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public class JDBC_SQLSERVER implements ITestConnector
{
	@Override
	public void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(MSSqlConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(MSSqlDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(MSSqlTestDialect.class).autowireable(IConnectionTestDialect.class);
	}

	@Override
	public void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(MSSqlTestModule.class);
	}
}
