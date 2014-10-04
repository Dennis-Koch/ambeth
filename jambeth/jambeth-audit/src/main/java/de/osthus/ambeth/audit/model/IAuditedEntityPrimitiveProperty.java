package de.osthus.ambeth.audit.model;

public interface IAuditedEntityPrimitiveProperty
{
	public static final String Order = "Order";

	public static final String Name = "Name";

	public static final String NewValue = "NewValue";

	int getOrder();

	void setOrder(int order);

	String getName();

	void setName(String name);

	Object getNewValue();

	void setNewValue(Object newValue);
}
