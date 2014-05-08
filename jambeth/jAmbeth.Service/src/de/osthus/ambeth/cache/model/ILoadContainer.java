package de.osthus.ambeth.cache.model;

import de.osthus.ambeth.merge.model.IObjRef;

public interface ILoadContainer
{
	IObjRef getReference();

	Object[] getPrimitives();

	void setPrimitives(Object[] primitives);

	IObjRef[][] getRelations();

}
