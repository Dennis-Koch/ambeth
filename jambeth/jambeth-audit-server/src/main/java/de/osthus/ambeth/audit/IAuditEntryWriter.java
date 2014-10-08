package de.osthus.ambeth.audit;

import java.io.DataOutputStream;

import de.osthus.ambeth.audit.model.IAuditEntry;

public interface IAuditEntryWriter
{
	void writeAuditEntry(IAuditEntry auditEntry, DataOutputStream os) throws Throwable;
}
