package com.koch.ambeth.merge.cache;

import java.util.Collection;

public interface HandleChildCachesDelegate
{
	void invoke(Collection<IWritableCache> childCaches);
}
