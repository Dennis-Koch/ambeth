package de.osthus.ambeth.merge.model;

public class PostLoadMethodLifecycleExtension extends AbstractMethodLifecycleExtension
{
	@Override
	public void postLoad(Object entity)
	{
		callMethod(entity, "PostLoad");
	}

	@Override
	public void prePersist(Object entity)
	{
		// intended blank
	}
}
