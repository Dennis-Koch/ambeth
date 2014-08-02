package de.osthus.ambeth.merge.model;

public class PrePersistMethodLifecycleExtension extends AbstractMethodLifecycleExtension
{
	@Override
	public void postCreate(IEntityMetaData metaData, Object newEntity)
	{
		// intended blank
	}

	@Override
	public void postLoad(IEntityMetaData metaData, Object entity)
	{
		// intended blank
	}

	@Override
	public void prePersist(IEntityMetaData metaData, Object entity)
	{
		callMethod(entity, "PrePersist");
	}
}
