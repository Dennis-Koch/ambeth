package de.osthus.ambeth.audit.model;

import java.util.List;

public interface IAuditedEntity
{
	public static final String EntityId = "EntityId";

	public static final String EntityType = "EntityType";

	public static final String ChangeType = "ChangeType";

	public static final String Primitives = "Primitives";

	public static final String Relations = "Relations";

	public static final String EntityVersion = "EntityVersion";

	int getOrder();

	void setOrder(int order);

	Object getEntityId();

	void setEntityId(Object entityId);

	Class<?> getEntityType();

	void setEntityType(Class<?> entityType);

	Object getEntityVersion();

	void setEntityVersion(Object entityVersion);

	AuditedEntityChangeType getChangeType();

	void setChangeType(AuditedEntityChangeType changeType);

	List<? extends IAuditedEntityPrimitiveProperty> getPrimitives();

	List<? extends IAuditedEntityRelationProperty> getRelations();
}
