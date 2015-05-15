package de.osthus.ambeth.audit;

import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IAuditEntryReader
{
	List<IAuditedEntity> getAllAuditedEntitiesOfEntity(Object entity);

	List<IAuditedEntity> getAllAuditedEntitiesOfEntity(IObjRef objRef);

	List<IAuditEntry> getAllAuditEntriesOfEntity(Object entity);

	List<IAuditEntry> getAllAuditEntriesOfEntity(IObjRef objRef);
}