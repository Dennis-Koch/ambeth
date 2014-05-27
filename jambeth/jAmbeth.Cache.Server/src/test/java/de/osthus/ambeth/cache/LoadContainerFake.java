package de.osthus.ambeth.cache;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.merge.model.IObjRef;

public class LoadContainerFake implements ILoadContainer
{
	public IObjRef reference;
	public Object[] primitives;
	public IObjRef[][] relations;

	public LoadContainerFake(IObjRef reference, Object[] primitives, IObjRef[][] relations)
	{
		this.reference = reference;
		this.primitives = primitives;
		this.relations = relations;
	}

	@Override
	public IObjRef getReference()
	{
		return this.reference;
	}

	@Override
	public Object[] getPrimitives()
	{
		return this.primitives;
	}

	@Override
	public void setPrimitives(Object[] primitives)
	{
		this.primitives = primitives;
	}

	@Override
	public IObjRef[][] getRelations()
	{
		return this.relations;
	}

	public void setRelations(IObjRef[][] relations)
	{
		this.relations = relations;
	}
}
