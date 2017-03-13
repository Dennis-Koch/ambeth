package com.koch.ambeth.security.persistence.ioc;

import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.event.IEntityMetaDataEvent;
import com.koch.ambeth.query.IQueryBuilderExtensionExtendable;
import com.koch.ambeth.security.persistence.IPermissionGroupUpdater;
import com.koch.ambeth.security.persistence.PermissionGroupUpdater;
import com.koch.ambeth.security.persistence.SecurityQueryBuilderExtension;
import com.koch.ambeth.security.persistence.UpdatePermissionGroupEventListener;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleEvent;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

@FrameworkModule
public class SecurityQueryModule implements IInitializingModule {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		IBeanConfiguration permissionGroupUpdater = beanContextFactory
				.registerBean(PermissionGroupUpdater.class).autowireable(IPermissionGroupUpdater.class);
		beanContextFactory.link(permissionGroupUpdater, "handleEntityMetaDataEvent")
				.to(IEventListenerExtendable.class).with(IEntityMetaDataEvent.class);
		beanContextFactory.link(permissionGroupUpdater, "handleEntityPermissionRuleEvent")
				.to(IEventListenerExtendable.class).with(IEntityPermissionRuleEvent.class);
		beanContextFactory.link(permissionGroupUpdater, "handleClearAllCachesEvent")
				.to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		if (securityActive) {
			IBeanConfiguration securityQueryBuilderExtension =
					beanContextFactory.registerBean(SecurityQueryBuilderExtension.class);
			beanContextFactory.link(securityQueryBuilderExtension)
					.to(IQueryBuilderExtensionExtendable.class);

			IBeanConfiguration updatePermissionGroupEventListener =
					beanContextFactory.registerBean(UpdatePermissionGroupEventListener.class);
			beanContextFactory.link(updatePermissionGroupEventListener, "handleDataChangeOfSession")
					.to(IEventListenerExtendable.class).with(IDataChangeOfSession.class);
		}
	}
}
