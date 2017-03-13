package com.koch.ambeth.cache.proxy;

import com.koch.ambeth.cache.ICacheIntern;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.merge.model.IObjRef;

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
