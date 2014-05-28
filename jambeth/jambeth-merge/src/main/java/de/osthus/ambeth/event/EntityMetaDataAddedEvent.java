package de.osthus.ambeth.event;

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
