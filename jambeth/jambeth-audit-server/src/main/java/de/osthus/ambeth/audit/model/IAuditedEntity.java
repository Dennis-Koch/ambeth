package de.osthus.ambeth.audit.model;

import java.util.List;

public interface IAuditedEntity
{
	public static final String ChangeType = "ChangeType";

	public static final String Entry = "Entry";

	public static final String Order = "Order";

	public static final String Primitives = "Primitives";

	public static final String Ref = "Ref";

	public static final String Relations = "Relations";

	AuditedEntityChangeType getChangeType();

	IAuditEntry getEntry();

	int getOrder();

	List<? extends IAuditedEntityPrimitiveProperty> getPrimitives();

	IAuditedEntityRef getRef();

	List<? extends IAuditedEntityRelationProperty> getRelations();
}
