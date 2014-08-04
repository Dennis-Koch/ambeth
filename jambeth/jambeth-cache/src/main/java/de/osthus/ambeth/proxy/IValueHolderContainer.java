package de.osthus.ambeth.proxy;

import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IValueHolderContainer extends IObjRefContainer
{
	IObjRelation get__Self(int relationIndex);

	ICacheIntern get__TargetCache();

	void set__TargetCache(ICacheIntern targetCache);

	Object get__ValueDirect(int relationIndex);

	void set__ValueDirect(int relationIndex, Object value);

	void set__InitPending(int relationIndex);

	void set__Uninitialized(int relationIndex, IObjRef[] objRefs);
}
