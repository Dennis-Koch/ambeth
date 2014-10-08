package de.osthus.ambeth.audit.model;

import java.util.List;

public interface IAuditedEntityRelationProperty
{
	public static final String Order = "Order";

	public static final String Name = "Name";

	public static final String Items = "Items";

	int getOrder();

	void setOrder(int order);

	String getName();

	void setName(String name);

	List<? extends IAuditedEntityRelationPropertyItem> getItems();
}
