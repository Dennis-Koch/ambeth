package de.osthus.ambeth.merge.incremental;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;

public interface IIncrementalMergeState
{
	ICache getStateCache();

	CreateOrUpdateContainerBuild newCreateContainer(Class<?> entityType);

	CreateOrUpdateContainerBuild newUpdateContainer(Class<?> entityType);
}
