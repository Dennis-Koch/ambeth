package de.osthus.ambeth.ioc;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.services.DataChangeEventDAO;
import de.osthus.ambeth.services.DataChangeEventService;
import de.osthus.ambeth.services.DataChangePersistenceListener;
import de.osthus.ambeth.services.IDataChangeEventDAO;
import de.osthus.ambeth.services.IDataChangeEventService;

public class DataChangePersistenceModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("dataChangeEventDAO", DataChangeEventDAO.class).autowireable(IDataChangeEventDAO.class);

		beanContextFactory.registerBean("dataChangeEventService", DataChangeEventService.class).autowireable(IDataChangeEventService.class);

		beanContextFactory.registerBean("dataChangePersistenceListener", DataChangePersistenceListener.class);
		beanContextFactory.link("dataChangePersistenceListener").to(IEventListenerExtendable.class).with(IDataChange.class);
	}
}
