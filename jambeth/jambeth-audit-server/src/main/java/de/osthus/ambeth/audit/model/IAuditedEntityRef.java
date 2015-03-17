package de.osthus.ambeth.audit.model;


public interface IAuditedEntityRef
{
	public static final String EntityId = "EntityId";

	public static final String EntityType = "EntityType";

	public static final String EntityVersion = "EntityVersion";

	Object getEntityId();

	void setEntityId(Object entityId);

	Class<?> getEntityType();

	void setEntityType(Class<?> entityType);

	Object getEntityVersion();

	void setEntityVersion(Object entityVersion);
}
