package de.osthus.ambeth.persistence.callback;

import de.osthus.ambeth.IDatabaseLifecycleCallback;

public interface IDatabaseLifecycleCallbackRegistry
{
	IDatabaseLifecycleCallback[] getDatabaseLifecycleCallbacks();
}
