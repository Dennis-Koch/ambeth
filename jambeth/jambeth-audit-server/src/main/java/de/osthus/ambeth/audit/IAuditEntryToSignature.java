package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.security.model.ISignature;

public interface IAuditEntryToSignature
{
	void signAuditEntry(CreateOrUpdateContainerBuild auditEntry, char[] clearTextPassword, ISignature signature);

	byte[] createVerifyDigest(IAuditEntry auditEntry);

	byte[] createVerifyDigest(IAuditedEntity auditedEntity);
}