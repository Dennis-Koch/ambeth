package com.koch.ambeth.security.persistence;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class UpdatePermissionGroupEventListener
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPermissionGroupUpdater permissionGroupUpdater;

	public void handleDataChangeOfSession(IDataChangeOfSession dataChangeOfSession) throws Throwable
	{
		IDataChange dataChange = dataChangeOfSession.getDataChange();
		if (dataChange.isEmpty())
		{
			return;
		}
		permissionGroupUpdater.updatePermissionGroups(dataChange);
	}
}
