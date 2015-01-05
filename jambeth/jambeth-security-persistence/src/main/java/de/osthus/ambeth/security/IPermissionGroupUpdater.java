package de.osthus.ambeth.security;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface IPermissionGroupUpdater
{
	void updatePermissionGroups(IDataChange dataChange);

	void fillEmptyPermissionGroups();

	<R> R executeWithoutPermissionGroupUpdate(IResultingBackgroundWorkerDelegate<R> runnable);
}