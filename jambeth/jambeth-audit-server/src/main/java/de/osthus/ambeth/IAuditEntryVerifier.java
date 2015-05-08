package de.osthus.ambeth;

import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IAuditEntryVerifier
{
	boolean verifyEntities(List<? extends IObjRef> objRefs);

	boolean[] verifyAuditEntries(List<? extends IAuditEntry> auditEntries);

	boolean[] verifyAuditedEntities(List<? extends IAuditedEntity> auditedEntities);
}