package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.h2.H2ConnectionUrlProvider;
import de.osthus.ambeth.h2.H2Dialect;
import de.osthus.ambeth.h2.H2TestDialect;
import de.osthus.ambeth.h2.H2TestModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public class JDBC_H2 implements ITestConnector
{
	@Override
	public void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(H2ConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(H2Dialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(H2TestDialect.class).autowireable(IConnectionTestDialect.class);
	}

	@Override
	public void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(H2TestModule.class);
	}
}
