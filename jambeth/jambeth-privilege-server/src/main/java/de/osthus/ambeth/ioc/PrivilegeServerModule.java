package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtensionExtendable;
import de.osthus.ambeth.privilege.IPrivilegeRegistry;
import de.osthus.ambeth.privilege.service.IPrivilegeService;
import de.osthus.ambeth.privilege.service.PrivilegeService;

@FrameworkModule
public class PrivilegeServerModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("privilegeService", PrivilegeService.class).autowireable(IPrivilegeService.class);

		beanContextFactory.registerBean("privilegeRegistry", ExtendableBean.class)
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IPrivilegeProviderExtensionExtendable.class)
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IPrivilegeRegistry.class)
				.autowireable(IPrivilegeRegistry.class, IPrivilegeProviderExtensionExtendable.class);
	}
}
