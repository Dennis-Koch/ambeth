package de.osthus.ambeth.audit.model;

public interface IAuditedEntityRelationPropertyItem
{
	public static final String ChangeType = "ChangeType";

	public static final String Order = "Order";

	public static final String Ref = "Ref";

	int getOrder();

	void setOrder(int order);

	IAuditedEntityRef getRef();

	void setRef(IAuditedEntityRef ref);

	AuditedEntityPropertyItemChangeType getChangeType();

	void setChangeType(AuditedEntityPropertyItemChangeType changeType);

}
