package com.koch.ambeth.cache.rootcachevalue;

import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;

public class DefaultRootCacheValue extends RootCacheValue
{
	protected Object[] primitives;

	protected IObjRef[][] relations;

	protected Object id;

	protected Object version;

	protected IEntityMetaData metaData;

	public DefaultRootCacheValue(IEntityMetaData metaData)
	{
		super(metaData);
		this.metaData = metaData;
	}

	@Override
	public IEntityMetaData get__EntityMetaData()
	{
		return metaData;
	}

	@Override
	public Object getId()
	{
		return id;
	}

	@Override
	public void setId(Object id)
	{
		this.id = id;
	}

	@Override
	public Object getVersion()
	{
		return version;
	}

	@Override
	public void setVersion(Object version)
	{
		this.version = version;
	}

	@Override
	public void setPrimitives(Object[] primitives)
	{
		this.primitives = primitives;
	}

	@Override
	public Object[] getPrimitives()
	{
		return primitives;
	}

	@Override
	public Object getPrimitive(int primitiveIndex)
	{
		return primitives[primitiveIndex];
	}

	@Override
	public IObjRef[][] getRelations()
	{
		return relations;
	}

	@Override
	public void setRelations(IObjRef[][] relations)
	{
		this.relations = relations;
	}

	@Override
	public IObjRef[] getRelation(int relationIndex)
	{
		return relations[relationIndex];
	}

	@Override
	public void setRelation(int relationIndex, IObjRef[] relationsOfMember)
	{
		relations[relationIndex] = relationsOfMember;
	}

	@Override
	public Class<?> getEntityType()
	{
		return metaData.getEntityType();
	}
}
