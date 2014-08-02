package de.osthus.ambeth.merge.model;

public class PostLoadMethodLifecycleExtension extends AbstractMethodLifecycleExtension
{
	@Override
	public void postCreate(IEntityMetaData metaData, Object newEntity)
	{
		// intended blank
	}

	@Override
	public void postLoad(IEntityMetaData metaData, Object entity)
	{
		callMethod(entity, "PostLoad");
	}

	@Override
	public void prePersist(IEntityMetaData metaData, Object entity)
	{
		// intended blank
	}
}
