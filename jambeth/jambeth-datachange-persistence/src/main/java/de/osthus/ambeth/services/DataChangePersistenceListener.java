package de.osthus.ambeth.services;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.DataChangeEntryBO;
import de.osthus.ambeth.model.DataChangeEventBO;
import de.osthus.ambeth.model.EntityType;
import de.osthus.ambeth.util.ParamChecker;

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
