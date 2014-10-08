package de.osthus.ambeth.audit.model;

public interface IAuditedEntityRelationPropertyItem
{
	public static final String Order = "Order";

	public static final String EntityId = "EntityId";

	public static final String EntityType = "EntityType";

	public static final String EntityVersion = "EntityVersion";

	public static final String ChangeType = "ChangeType";

	int getOrder();

	void setOrder(int order);

	Object getEntityId();

	void setEntityId(Object entityId);

	Class<?> getEntityType();

	void setEntityType(Class<?> entityType);

	Object getEntityVersion();

	void setEntityVersion(Object entityVersion);

	AuditedEntityPropertyItemChangeType getChangeType();

	void setChangeType(AuditedEntityPropertyItemChangeType changeType);

}
