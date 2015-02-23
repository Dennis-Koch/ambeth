package de.osthus.ambeth.ioc;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.datachange.UnfilteredDataChangeListener;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.IPrivilegeProviderIntern;
import de.osthus.ambeth.privilege.PrivilegeProvider;
import de.osthus.ambeth.privilege.factory.EntityPrivilegeFactoryProvider;
import de.osthus.ambeth.privilege.factory.EntityTypePrivilegeFactoryProvider;
import de.osthus.ambeth.privilege.factory.IEntityPrivilegeFactoryProvider;
import de.osthus.ambeth.privilege.factory.IEntityTypePrivilegeFactoryProvider;

@FrameworkModule
public class PrivilegeModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration privilegeProvider = beanContextFactory.registerBean(PrivilegeProvider.class).autowireable(IPrivilegeProvider.class,
				IPrivilegeProviderIntern.class);
		IBeanConfiguration ppEventListener = beanContextFactory.registerBean(UnfilteredDataChangeListener.class).propertyRefs(privilegeProvider);
		beanContextFactory.link(ppEventListener).to(IEventListenerExtendable.class).with(IDataChange.class);
		beanContextFactory.link(privilegeProvider, "handleClearAllCaches").to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		beanContextFactory.registerBean(EntityPrivilegeFactoryProvider.class).autowireable(IEntityPrivilegeFactoryProvider.class);
		beanContextFactory.registerBean(EntityTypePrivilegeFactoryProvider.class).autowireable(IEntityTypePrivilegeFactoryProvider.class);

		// if (IsNetworkClientMode && IsPrivilegeServiceBeanActive)
		// {
		// beanContextFactory.registerBean<ClientServiceBean>("privilegeServiceWCF")
		// .propertyValue("Interface", typeof(IPrivilegeService))
		// .propertyValue("SyncRemoteInterface", typeof(IPrivilegeServiceWCF))
		// .propertyValue("AsyncRemoteInterface", typeof(IPrivilegeClient)).autowireable<IPrivilegeService>();
		// }
	}
}
