package de.osthus.ambeth.cache.rootcachevalue;

import de.osthus.ambeth.merge.model.IObjRef;

public class DefaultRootCacheValue extends RootCacheValue
{
	protected final Class<?> entityType;

	protected Object[] primitives;

	protected IObjRef[][] relations;

	private Object id;

	private Object version;

	public DefaultRootCacheValue(Class<?> entityType)
	{
		super(entityType);
		this.entityType = entityType;
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
		return entityType;
	}
}
