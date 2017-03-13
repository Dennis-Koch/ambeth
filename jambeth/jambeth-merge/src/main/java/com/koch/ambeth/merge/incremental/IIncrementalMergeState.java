package com.koch.ambeth.merge.incremental;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;

public interface IIncrementalMergeState
{
	ICache getStateCache();

	CreateOrUpdateContainerBuild newCreateContainer(Class<?> entityType);

	CreateOrUpdateContainerBuild newUpdateContainer(Class<?> entityType);
}
