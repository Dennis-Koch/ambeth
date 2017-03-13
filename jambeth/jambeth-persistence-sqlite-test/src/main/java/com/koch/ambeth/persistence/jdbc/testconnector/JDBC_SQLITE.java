package com.koch.ambeth.persistence.jdbc.testconnector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.persistence.jdbc.testconnector.ITestConnector;
import com.koch.ambeth.persistence.sqlite.SQLiteConnectionUrlProvider;
import com.koch.ambeth.persistence.sqlite.SQLiteDialect;
import com.koch.ambeth.persistence.sqlite.SQLiteTestDialect;
import com.koch.ambeth.persistence.sqlite.SQLiteTestModule;

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
