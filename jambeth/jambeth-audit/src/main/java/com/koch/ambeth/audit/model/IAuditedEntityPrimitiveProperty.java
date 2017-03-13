package com.koch.ambeth.audit.model;

import com.koch.ambeth.security.audit.model.Audited;

@Audited(false)
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
