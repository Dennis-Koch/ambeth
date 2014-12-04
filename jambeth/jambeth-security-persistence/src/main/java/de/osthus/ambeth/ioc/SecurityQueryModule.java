package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IQueryBuilderExtensionExtendable;
import de.osthus.ambeth.security.SecurityQueryBuilderExtension;

@FrameworkModule
public class SecurityQueryModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration securityQueryBuilderExtension = beanContextFactory.registerBean(SecurityQueryBuilderExtension.class);
		beanContextFactory.link(securityQueryBuilderExtension).to(IQueryBuilderExtensionExtendable.class);
	}
}
