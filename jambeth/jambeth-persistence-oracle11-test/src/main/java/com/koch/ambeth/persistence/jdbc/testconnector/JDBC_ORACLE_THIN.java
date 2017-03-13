package com.koch.ambeth.persistence.jdbc.testconnector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.persistence.jdbc.testconnector.ITestConnector;
import com.koch.ambeth.persistence.oracle.Oracle10gDialect;
import com.koch.ambeth.persistence.oracle.Oracle10gTestDialect;
import com.koch.ambeth.persistence.oracle.Oracle10gTestModule;
import com.koch.ambeth.persistence.oracle.OracleConnectionUrlProvider;

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
