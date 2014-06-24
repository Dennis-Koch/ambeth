package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;

public interface IAuditEntryFactory
{
	IAuditEntry createAuditEntry();
}
