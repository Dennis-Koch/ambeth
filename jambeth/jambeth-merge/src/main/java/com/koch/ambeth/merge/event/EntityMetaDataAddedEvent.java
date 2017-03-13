package com.koch.ambeth.merge.event;

public class EntityMetaDataAddedEvent implements IEntityMetaDataEvent
{
	protected Class<?>[] entityTypes;

	public EntityMetaDataAddedEvent(Class<?>... entityTypes)
	{
		this.entityTypes = entityTypes;
	}

	@Override
	public Class<?>[] getEntityTypes()
	{
		return entityTypes;
	}
}
