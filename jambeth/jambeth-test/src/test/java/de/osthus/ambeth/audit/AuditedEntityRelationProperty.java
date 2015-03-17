package de.osthus.ambeth.audit;

import java.util.List;

import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;

public interface AuditedEntityRelationProperty extends IAbstractAuditEntity, IAuditedEntityRelationProperty
{
	@Override
	List<AuditedEntityRelationPropertyItem> getItems();
}
