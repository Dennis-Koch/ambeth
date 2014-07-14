package de.osthus.ambeth.testutil;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.oracle.Oracle10gThinDialect;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.connection.ConnectionFactory;
import de.osthus.ambeth.util.IPersistenceExceptionUtil;
import de.osthus.ambeth.util.PersistenceExceptionUtil;

@FrameworkModule
public class RandomUserModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(final IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("oracle10gThinDialect", Oracle10gThinDialect.class).autowireable(IConnectionDialect.class);
		beanContextFactory.registerBean("persistenceExceptionUtil", PersistenceExceptionUtil.class).autowireable(IPersistenceExceptionUtil.class);
		beanContextFactory.registerBean("connectionFactory", ConnectionFactory.class).autowireable(IConnectionFactory.class);
		beanContextFactory.registerBean("randomUserScript", RandomUserScript.class);
	}
}