package com.koch.ambeth.audit.server;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;

public interface IAuditEntryWriter
{
	byte[] writeAuditEntry(IAuditEntry auditEntry, String hashAlgorithm) throws Throwable;

	byte[] writeAuditedEntity(IAuditedEntity auditedEntity, String hashAlgorithm) throws Throwable;

	void writeAuditEntry(CreateOrUpdateContainerBuild auditEntry, String hashAlgorithm, java.security.Signature signature) throws Throwable;
}
