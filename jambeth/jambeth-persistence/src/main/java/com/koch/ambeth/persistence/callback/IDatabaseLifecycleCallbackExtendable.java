package com.koch.ambeth.persistence.callback;

import com.koch.ambeth.persistence.IDatabaseLifecycleCallback;

public interface IDatabaseLifecycleCallbackExtendable
{
	void registerDatabaseLifecycleCallback(IDatabaseLifecycleCallback databaseLifecycleCallback);

	void unregisterDatabaseLifecycleCallback(IDatabaseLifecycleCallback databaseLifecycleCallback);
}
