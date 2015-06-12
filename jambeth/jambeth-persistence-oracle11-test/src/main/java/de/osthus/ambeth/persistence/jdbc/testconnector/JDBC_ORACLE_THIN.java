package de.osthus.ambeth.persistence.jdbc.testconnector;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.oracle.Oracle10gDialect;
import de.osthus.ambeth.oracle.Oracle10gTestDialect;
import de.osthus.ambeth.oracle.Oracle10gTestModule;
import de.osthus.ambeth.oracle.OracleConnectionUrlProvider;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public class JDBC_ORACLE_THIN implements ITestConnector
{
	@Override
	public void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(OracleConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
		beanContextFactory.registerBean(Oracle10gDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean(Oracle10gTestDialect.class).autowireable(IConnectionTestDialect.class);
	}

	@Override
	public void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol)
	{
		beanContextFactory.registerBean(Oracle10gTestModule.class);
	}
}
