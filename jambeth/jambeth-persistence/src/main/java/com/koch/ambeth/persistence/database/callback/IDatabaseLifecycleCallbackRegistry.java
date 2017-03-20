package com.koch.ambeth.persistence.database.callback;

import com.koch.ambeth.persistence.database.IDatabaseLifecycleCallback;

public interface IDatabaseLifecycleCallbackRegistry {
	IDatabaseLifecycleCallback[] getDatabaseLifecycleCallbacks();
}
