package de.osthus.ambeth.security;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeOfSession;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class UpdatePermissionGroupEventListener
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPermissionGroupUpdater permissionGroupUpdater;

	public void handleDataChangeOfSession(IDataChangeOfSession dataChangeOfSession) throws Throwable
	{
		final IDataChange dataChange = dataChangeOfSession.getDataChange();
		if (dataChange.isEmpty())
		{
			return;
		}
		permissionGroupUpdater.insertPermissionGroups();
	}
}
