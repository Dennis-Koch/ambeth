package de.osthus.ambeth.cache;

import java.util.Collection;

public interface HandleChildCachesDelegate
{
	void invoke(Collection<IWritableCache> childCaches);
}
