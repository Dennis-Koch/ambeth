package de.osthus.ambeth.ioc;

import de.osthus.ambeth.datachange.UnfilteredDataChangeListener;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.PrivilegeProvider;

@FrameworkModule
public class PrivilegeModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("privilegeProvider", PrivilegeProvider.class)
		// .propertyRefs("privilegeServiceWCF")
				.autowireable(IPrivilegeProvider.class);
		beanContextFactory.registerBean("privilegeProvider_EventListener", UnfilteredDataChangeListener.class).propertyRefs("privilegeProvider");
		beanContextFactory.link("privilegeProvider_EventListener").to(IEventListenerExtendable.class).with(IDataChange.class);

		// if (IsNetworkClientMode && IsPrivilegeServiceBeanActive)
		// {
		// beanContextFactory.registerBean<ClientServiceBean>("privilegeServiceWCF")
		// .propertyValue("Interface", typeof(IPrivilegeService))
		// .propertyValue("SyncRemoteInterface", typeof(IPrivilegeServiceWCF))
		// .propertyValue("AsyncRemoteInterface", typeof(IPrivilegeClient)).autowireable<IPrivilegeService>();
		// }
	}
}
