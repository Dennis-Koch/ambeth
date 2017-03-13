package com.koch.ambeth.persistence.oracle;

import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.event.IEntityMetaDataEvent;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public class Oracle11gModule implements IInitializingModule
{
	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseChangeNotificationActive, defaultValue = "false")
	protected boolean databaseChangeNotificationActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (databaseChangeNotificationActive)
		{
			beanContextFactory.registerBean("oracleDatabaseChangeListener", OracleDatabaseChangeListener.class);

			beanContextFactory.registerBean("oracleDatabaseChangeRegistration", OracleDatabaseChangeRegistration.class).propertyRefs(
					"oracleDatabaseChangeListener");
			beanContextFactory.link("oracleDatabaseChangeRegistration").to(IEventListenerExtendable.class).with(IEntityMetaDataEvent.class);
		}
	}
}
