package com.koch.ambeth.audit;

import java.util.List;

import com.koch.ambeth.audit.model.IAuditEntry;

public interface AuditEntry extends IAbstractAuditEntity, IAuditEntry
{
	@Override
	List<AuditedService> getServices();

	@Override
	List<AuditedEntity> getEntities();
}
