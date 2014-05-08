package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.DefaultServiceFilter;
import de.osthus.ambeth.security.IEntityFilterExtendable;
import de.osthus.ambeth.security.ISecurityManager;
import de.osthus.ambeth.security.IServiceFilterExtendable;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.proxy.SecurityPostProcessor;

@FrameworkModule
public class SecurityServerModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = SecurityServerConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean isSecurityActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (isSecurityActive)
		{
			beanContextFactory.registerBean("securityPostProcessor", SecurityPostProcessor.class);

			beanContextFactory.registerBean("securityManager", de.osthus.ambeth.security.SecurityManager.class).autowireable(ISecurityManager.class,
					IEntityFilterExtendable.class, IServiceFilterExtendable.class);

			beanContextFactory.registerBean("defaultServiceFilter", DefaultServiceFilter.class);
			beanContextFactory.link("defaultServiceFilter").to(IServiceFilterExtendable.class);
		}
	}
}
