package com.koch.ambeth.datachange.persistence.services;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.persistence.model.DataChangeEntryBO;
import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.datachange.persistence.model.EntityType;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;

public class DataChangePersistenceListener implements IEventListener, IInitializingBean
{
	private static final Class<?>[] uninterestingTypes = { DataChangeEventBO.class, DataChangeEntryBO.class, EntityType.class };

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IDataChangeEventService dataChangeEventService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(dataChangeEventService, "dataChangeEventService");
	}

	public void setDataChangeEventService(IDataChangeEventService dataChangeEventService)
	{
		this.dataChangeEventService = dataChangeEventService;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (!(eventObject instanceof IDataChange))
		{
			return;
		}

		IDataChange dataChange = (IDataChange) eventObject;
		dataChange = dataChange.deriveNot(uninterestingTypes);
		if (dataChange.getAll().isEmpty())
		{
			return;
		}

		dataChangeEventService.save(dataChange);
	}
}
