package com.koch.ambeth.service.cache.model;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface ILoadContainer
{
	IObjRef getReference();

	Object[] getPrimitives();

	void setPrimitives(Object[] primitives);

	IObjRef[][] getRelations();

}
