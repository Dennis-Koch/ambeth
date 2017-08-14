package com.koch.ambeth.merge.cache;

import com.koch.ambeth.util.state.IStateRollback;

public interface ICacheContext {
	IStateRollback pushCache(ICache cache, IStateRollback... rollbacks);

	IStateRollback pushCache(ICacheProvider cacheProvider, IStateRollback... rollbacks);
}
