package de.osthus.ambeth.audit;

import java.util.List;

import de.osthus.ambeth.audit.model.IAuditedEntity;

public interface AuditedEntity extends IAbstractAuditEntity, IAuditedEntity
{
	@Override
	List<AuditedEntityPrimitiveProperty> getPrimitives();

	@Override
	List<AuditedEntityRelationProperty> getRelations();
}
