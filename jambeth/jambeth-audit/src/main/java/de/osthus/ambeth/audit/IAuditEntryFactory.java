package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import de.osthus.ambeth.audit.model.IAuditedService;
import de.osthus.ambeth.security.model.ISignature;

public interface IAuditEntryFactory
{
	IAuditEntry createAuditEntry();

	IAuditedService createAuditedService();

	IAuditedEntity createAuditedEntity();

	IAuditedEntityPrimitiveProperty createAuditedEntityPrimitiveProperty();

	IAuditedEntityRelationProperty createAuditedEntityRelationProperty();

	IAuditedEntityRelationPropertyItem createAuditedEntityRelationPropertyItem();

	ISignature createSignature();
}
