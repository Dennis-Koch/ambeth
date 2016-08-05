package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import de.osthus.ambeth.sqlite.SQLiteConnectionUrlProvider;
import de.osthus.ambeth.sqlite.SQLiteDialect;
import de.osthus.ambeth.sqlite.SQLiteTestDialect;
import de.osthus.ambeth.sqlite.SQLiteTestModule;

public class JDBC_SQLITE implements ITestConnector
{
	@Override
	public void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(SQLiteConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(SQLiteDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(SQLiteTestDialect.class).autowireable(IConnectionTestDialect.class);
	}

	@Override
	public void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(SQLiteTestModule.class);
	}
}
