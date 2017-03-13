package com.koch.ambeth.audit.server;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.security.model.ISignature;

public interface IAuditEntryToSignature
{
	void signAuditEntry(CreateOrUpdateContainerBuild auditEntry, char[] clearTextPassword, ISignature signature);

	byte[] createVerifyDigest(IAuditEntry auditEntry, java.security.Signature signature);

	byte[] createVerifyDigest(IAuditedEntity auditedEntity);
}