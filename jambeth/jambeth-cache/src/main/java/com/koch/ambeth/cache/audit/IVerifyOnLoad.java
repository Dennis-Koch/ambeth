package com.koch.ambeth.cache.audit;

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface IVerifyOnLoad
{
	<R> R verifyEntitiesOnLoad(IResultingBackgroundWorkerDelegate<R> runnable) throws Throwable;

	void queueVerifyEntitiesOnLoad(IList<ILoadContainer> loadedEntities);
}
