package de.osthus.ambeth.oracle;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;

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
