package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtensionExtendable;
import de.osthus.ambeth.service.IPrivilegeService;
import de.osthus.ambeth.service.PrivilegeService;

@FrameworkModule
public class PrivilegeServerModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("privilegeService", PrivilegeService.class).autowireable(IPrivilegeService.class,
				IPrivilegeProviderExtensionExtendable.class);

	}
}
