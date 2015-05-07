package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.security.model.ISignature;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface IAuditEntryToSignature
{

	void signAuditEntry(CreateOrUpdateContainerBuild auditEntry, char[] clearTextPassword, ISignature signature);

	void writeToSignatureHandle(java.security.Signature signatureHandle, IAuditEntry auditEntry, CreateOrUpdateContainerBuild auditEntryContainer);

}