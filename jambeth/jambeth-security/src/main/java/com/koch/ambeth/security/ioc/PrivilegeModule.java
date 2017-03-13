package com.koch.ambeth.security.ioc;

import com.koch.ambeth.datachange.UnfilteredDataChangeListener;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.privilege.IPrivilegeProvider;
import com.koch.ambeth.security.privilege.IPrivilegeProviderIntern;
import com.koch.ambeth.security.privilege.PrivilegeProvider;
import com.koch.ambeth.security.privilege.factory.EntityPrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.EntityTypePrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.IEntityPrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.IEntityTypePrivilegeFactoryProvider;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

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
