package de.osthus.ambeth.audit;

import java.io.DataOutputStream;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;

public interface IAuditEntryWriter
{
	void writeAuditEntry(IAuditEntry auditEntry, DataOutputStream os) throws Throwable;

	void writeAuditEntry(CreateOrUpdateContainerBuild auditEntry, DataOutputStream os) throws Throwable;
}
