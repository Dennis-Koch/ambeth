package com.koch.ambeth.merge.proxy;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IObjRefContainer extends IEntityMetaDataHolder
{
	ICache get__Cache();

	void detach();

	ValueHolderState get__State(int relationIndex);

	boolean is__Initialized(int relationIndex);

	IObjRef[] get__ObjRefs(int relationIndex);

	void set__ObjRefs(int relationIndex, IObjRef[] objRefs);
}
