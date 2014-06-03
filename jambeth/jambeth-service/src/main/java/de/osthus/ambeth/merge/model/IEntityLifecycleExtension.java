package de.osthus.ambeth.merge.model;

public interface IEntityLifecycleExtension
{
	void postLoad(Object entity);

	void prePersist(Object entity);
}
