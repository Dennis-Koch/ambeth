package com.koch.ambeth.audit;

import java.util.List;

import com.koch.ambeth.audit.model.IAuditedEntity;

public interface AuditedEntity extends IAbstractAuditEntity, IAuditedEntity
{
	@Override
	List<AuditedEntityPrimitiveProperty> getPrimitives();

	@Override
	List<AuditedEntityRelationProperty> getRelations();
}
