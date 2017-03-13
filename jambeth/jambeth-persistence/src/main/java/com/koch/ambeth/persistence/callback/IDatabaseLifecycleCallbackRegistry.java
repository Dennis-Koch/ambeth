package com.koch.ambeth.persistence.callback;

import com.koch.ambeth.persistence.IDatabaseLifecycleCallback;

public interface IDatabaseLifecycleCallbackRegistry
{
	IDatabaseLifecycleCallback[] getDatabaseLifecycleCallbacks();
}
