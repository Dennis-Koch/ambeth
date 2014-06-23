package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeSecurityManager;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtensionExtendable;
import de.osthus.ambeth.security.DefaultServiceFilter;
import de.osthus.ambeth.security.IActionPermission;
import de.osthus.ambeth.security.ISecurityManager;
import de.osthus.ambeth.security.IServiceFilterExtendable;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.security.privilegeprovider.ActionPermissionPrivilegeProviderExtension;
import de.osthus.ambeth.security.proxy.SecurityPostProcessor;

@FrameworkModule
public class SecurityServerModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = SecurityConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean isSecurityActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (isSecurityActive)
		{
			beanContextFactory.registerAnonymousBean(SecurityPostProcessor.class);

			beanContextFactory.registerAnonymousBean(de.osthus.ambeth.security.SecurityManager.class).autowireable(ISecurityManager.class,
					IMergeSecurityManager.class, IServiceFilterExtendable.class);

			IBeanConfiguration actionPermissionExtensionBC = beanContextFactory.registerAnonymousBean(ActionPermissionPrivilegeProviderExtension.class);
			beanContextFactory.link(actionPermissionExtensionBC).to(IPrivilegeProviderExtensionExtendable.class).with(IActionPermission.class);

			IBeanConfiguration defaultServiceFilterBC = beanContextFactory.registerAnonymousBean(DefaultServiceFilter.class);
			beanContextFactory.link(defaultServiceFilterBC).to(IServiceFilterExtendable.class);
		}
	}
}
