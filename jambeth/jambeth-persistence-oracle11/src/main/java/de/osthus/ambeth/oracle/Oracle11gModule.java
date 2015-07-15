package de.osthus.ambeth.oracle;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IEntityMetaDataEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

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
