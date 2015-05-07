package de.osthus.ambeth.audit.model;

public interface IAuditedEntityPrimitiveProperty
{
	public static final String Entity = "Entity";

	public static final String Order = "Order";

	public static final String Name = "Name";

	public static final String NewValue = "NewValue";

	IAuditedEntity getEntity();

	int getOrder();

	String getName();

	String getNewValue();
}
