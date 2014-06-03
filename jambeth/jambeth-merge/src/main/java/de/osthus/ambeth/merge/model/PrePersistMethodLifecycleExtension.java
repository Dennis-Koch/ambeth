package de.osthus.ambeth.merge.model;

public class PrePersistMethodLifecycleExtension extends AbstractMethodLifecycleExtension
{
	@Override
	public void postLoad(Object entity)
	{
		// intended blank
	}

	@Override
	public void prePersist(Object entity)
	{
		callMethod(entity, "PrePersist");
	}
}
