package com.koch.ambeth.audit.model;

import java.util.List;

import com.koch.ambeth.security.audit.model.Audited;

@Audited(false)
public interface IAuditedEntityRelationProperty
{
	public static final String Entity = "Entity";

	public static final String Items = "Items";

	public static final String Name = "Name";

	public static final String Order = "Order";

	IAuditedEntity getEntity();

	int getOrder();

	String getName();

	List<? extends IAuditedEntityRelationPropertyItem> getItems();
}
