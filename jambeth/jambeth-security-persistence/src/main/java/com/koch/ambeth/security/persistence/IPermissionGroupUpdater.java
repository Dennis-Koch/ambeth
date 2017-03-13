package com.koch.ambeth.security.persistence;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface IPermissionGroupUpdater
{
	void updatePermissionGroups(IDataChange dataChange);

	void fillEmptyPermissionGroups();

	<R> R executeWithoutPermissionGroupUpdate(IResultingBackgroundWorkerDelegate<R> runnable);
}