package de.osthus.ambeth.audit.model;

public interface IAuditedEntityRef
{
	public static final String EntityId = "EntityId";

	public static final String EntityType = "EntityType";

	public static final String EntityVersion = "EntityVersion";

	Object getEntityId();

	Class<?> getEntityType();

	Object getEntityVersion();
}
