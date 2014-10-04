package de.osthus.ambeth.audit;

import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;

public interface AuditEntry extends IAbstractAuditEntity, IAuditEntry
{
	@Override
	List<AuditedService> getServices();

	@Override
	List<AuditedEntity> getEntities();
}
