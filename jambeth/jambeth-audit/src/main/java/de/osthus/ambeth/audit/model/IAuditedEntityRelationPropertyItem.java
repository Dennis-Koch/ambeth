package de.osthus.ambeth.audit.model;


@Audited(false)
public interface IAuditedEntityRelationPropertyItem
{
	public static final String ChangeType = "ChangeType";

	public static final String Order = "Order";

	public static final String Ref = "Ref";

	int getOrder();

	IAuditedEntityRef getRef();

	AuditedEntityPropertyItemChangeType getChangeType();
}
