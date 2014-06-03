package de.osthus.ambeth.merge.model;

public interface IEntityLifecycleExtendable
{
	void registerEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension, Class<?> entityType);

	void unregisterEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension, Class<?> entityType);
}
