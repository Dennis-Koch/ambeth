package com.koch.ambeth.merge.event;

public class EntityMetaDataRemovedEvent implements IEntityMetaDataEvent
{
	protected Class<?>[] entityTypes;

	public EntityMetaDataRemovedEvent(Class<?>... entityTypes)
	{
		this.entityTypes = entityTypes;
	}

	@Override
	public Class<?>[] getEntityTypes()
	{
		return entityTypes;
	}
}
