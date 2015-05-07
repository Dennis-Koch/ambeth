package de.osthus.ambeth.audit;

import java.util.List;

import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IAuditEntryReader
{
	List<IAuditedEntity> getAllAuditedEntitiesOfEntity(IObjRef objRef);
}