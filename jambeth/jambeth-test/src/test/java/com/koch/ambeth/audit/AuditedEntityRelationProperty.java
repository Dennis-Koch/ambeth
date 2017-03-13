package com.koch.ambeth.audit;

import java.util.List;

import com.koch.ambeth.audit.model.IAuditedEntityRelationProperty;

public interface AuditedEntityRelationProperty extends IAbstractAuditEntity, IAuditedEntityRelationProperty
{
	@Override
	List<AuditedEntityRelationPropertyItem> getItems();
}
