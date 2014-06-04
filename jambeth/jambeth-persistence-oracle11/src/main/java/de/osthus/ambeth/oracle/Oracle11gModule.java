package de.osthus.ambeth.oracle;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IEntityMetaDataEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

@FrameworkModule
public class Oracle11gModule implements IInitializingModule
{
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

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseChangeNotificationActive, defaultValue = "false")
	public void setDatabaseChangeNotificationActive(boolean databaseChangeNotificationActive)
	{
		this.databaseChangeNotificationActive = databaseChangeNotificationActive;
	}
}