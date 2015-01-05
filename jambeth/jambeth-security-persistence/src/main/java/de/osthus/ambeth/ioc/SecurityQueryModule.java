package de.osthus.ambeth.ioc;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.datachange.model.IDataChangeOfSession;
import de.osthus.ambeth.event.IEntityMetaDataEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.privilege.IEntityPermissionRuleEvent;
import de.osthus.ambeth.query.IQueryBuilderExtensionExtendable;
import de.osthus.ambeth.security.IPermissionGroupUpdater;
import de.osthus.ambeth.security.PermissionGroupUpdater;
import de.osthus.ambeth.security.SecurityQueryBuilderExtension;
import de.osthus.ambeth.security.UpdatePermissionGroupEventListener;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;

@FrameworkModule
public class SecurityQueryModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = SecurityConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration permissionGroupUpdater = beanContextFactory.registerBean(PermissionGroupUpdater.class).autowireable(IPermissionGroupUpdater.class);
		beanContextFactory.link(permissionGroupUpdater, "handleEntityMetaDataEvent").to(IEventListenerExtendable.class).with(IEntityMetaDataEvent.class);
		beanContextFactory.link(permissionGroupUpdater, "handleEntityPermissionRuleEvent").to(IEventListenerExtendable.class)
				.with(IEntityPermissionRuleEvent.class);
		beanContextFactory.link(permissionGroupUpdater, "handleClearAllCachesEvent").to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		if (securityActive)
		{
			IBeanConfiguration securityQueryBuilderExtension = beanContextFactory.registerBean(SecurityQueryBuilderExtension.class);
			beanContextFactory.link(securityQueryBuilderExtension).to(IQueryBuilderExtensionExtendable.class);

			IBeanConfiguration updatePermissionGroupEventListener = beanContextFactory.registerBean(UpdatePermissionGroupEventListener.class);
			beanContextFactory.link(updatePermissionGroupEventListener, "handleDataChangeOfSession").to(IEventListenerExtendable.class)
					.with(IDataChangeOfSession.class);
		}
	}
}
