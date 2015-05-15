package de.osthus.ambeth.audit;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface IVerifyOnLoad
{
	<R> R verifyEntitiesOnLoad(IResultingBackgroundWorkerDelegate<R> runnable) throws Throwable;

	void queueVerifyEntitiesOnLoad(IList<ILoadContainer> loadedEntities);
}
