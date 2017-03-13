package com.koch.ambeth.datachange.persistence.ioc;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.persistence.services.DataChangeEventDAO;
import com.koch.ambeth.datachange.persistence.services.DataChangeEventService;
import com.koch.ambeth.datachange.persistence.services.DataChangePersistenceListener;
import com.koch.ambeth.datachange.persistence.services.IDataChangeEventDAO;
import com.koch.ambeth.datachange.persistence.services.IDataChangeEventService;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

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
