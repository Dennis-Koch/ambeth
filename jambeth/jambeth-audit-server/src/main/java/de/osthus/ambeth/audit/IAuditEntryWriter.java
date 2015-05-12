package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;

public interface IAuditEntryWriter
{
	byte[] writeAuditEntry(IAuditEntry auditEntry, String hashAlgorithm) throws Throwable;

	byte[] writeAuditedEntity(IAuditedEntity auditedEntity, String hashAlgorithm) throws Throwable;

	void writeAuditEntry(CreateOrUpdateContainerBuild auditEntry, String hashAlgorithm, java.security.Signature signature) throws Throwable;
}
