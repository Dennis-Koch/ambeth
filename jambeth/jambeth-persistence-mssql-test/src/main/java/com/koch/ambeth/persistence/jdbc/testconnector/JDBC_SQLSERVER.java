package com.koch.ambeth.persistence.jdbc.testconnector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.persistence.jdbc.testconnector.ITestConnector;
import com.koch.ambeth.persistence.mssql.MSSqlConnectionUrlProvider;
import com.koch.ambeth.persistence.mssql.MSSqlDialect;
import com.koch.ambeth.persistence.mssql.MSSqlTestDialect;
import com.koch.ambeth.persistence.mssql.MSSqlTestModule;

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
