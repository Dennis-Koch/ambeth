package de.osthus.ambeth.persistence.callback;

import de.osthus.ambeth.IDatabaseLifecycleCallback;

public interface IDatabaseLifecycleCallbackExtendable
{
	void registerDatabaseLifecycleCallback(IDatabaseLifecycleCallback databaseLifecycleCallback);

	void unregisterDatabaseLifecycleCallback(IDatabaseLifecycleCallback databaseLifecycleCallback);
}
