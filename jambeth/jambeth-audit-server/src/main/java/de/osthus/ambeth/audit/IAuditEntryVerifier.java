package de.osthus.ambeth.audit;

import java.util.Collection;

import de.osthus.ambeth.audit.model.IAuditEntry;

public interface IAuditEntryVerifier
{
	boolean verifyAuditEntries(Collection<? extends IAuditEntry> auditEntries);
}