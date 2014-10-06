package de.osthus.ambeth.audit;


public interface IAuditEntryWriterExtendable
{
	void registerAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion);

	void unregisterAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion);
}
