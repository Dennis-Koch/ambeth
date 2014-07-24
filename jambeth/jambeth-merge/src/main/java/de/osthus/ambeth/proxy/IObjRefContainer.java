package de.osthus.ambeth.proxy;

import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IObjRefContainer extends IEntityMetaDataHolder
{
	ValueHolderState get__State(int relationIndex);

	void set__InitPending(int relationIndex);

	boolean is__Initialized(int relationIndex);

	IObjRef[] get__ObjRefs(int relationIndex);

	void set__ObjRefs(int relationIndex, IObjRef[] objRefs);

	void set__Uninitialized(int relationIndex, IObjRef[] objRefs);
}
