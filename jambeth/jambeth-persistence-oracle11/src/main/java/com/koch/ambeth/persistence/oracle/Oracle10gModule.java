package com.koch.ambeth.persistence.oracle;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;

public class Oracle10gModule extends Oracle10gSimpleModule
{
	public static boolean handlesDatabaseProtocol(String databaseProtocol)
	{
		return databaseProtocol.toLowerCase().startsWith("jdbc:oracle");
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		super.afterPropertiesSet(beanContextFactory);

		beanContextFactory.registerBean("oracleConnectionExtension", OracleConnectionExtension.class).autowireable(IConnectionExtension.class);
	}
}
