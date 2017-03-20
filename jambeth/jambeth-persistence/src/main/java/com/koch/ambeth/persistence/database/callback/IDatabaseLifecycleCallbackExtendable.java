package com.koch.ambeth.persistence.database.callback;

import com.koch.ambeth.persistence.database.IDatabaseLifecycleCallback;

public interface IDatabaseLifecycleCallbackExtendable {
	void registerDatabaseLifecycleCallback(IDatabaseLifecycleCallback databaseLifecycleCallback);

	void unregisterDatabaseLifecycleCallback(IDatabaseLifecycleCallback databaseLifecycleCallback);
}
