package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.maria.MariaConnectionUrlProvider;
import de.osthus.ambeth.maria.MariaDialect;
import de.osthus.ambeth.maria.MariaTestDialect;
import de.osthus.ambeth.maria.MariaTestModule;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

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
