package de.osthus.ambeth.audit.model;

@Audited(false)
public interface IAuditedEntityRef
{
	public static final String EntityId = "EntityId";

	public static final String EntityType = "EntityType";

	public static final String EntityVersion = "EntityVersion";

	String getEntityId();

	Class<?> getEntityType();

	String getEntityVersion();
}
