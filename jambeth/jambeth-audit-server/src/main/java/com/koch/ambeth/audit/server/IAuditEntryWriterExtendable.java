package com.koch.ambeth.audit.server;


public interface IAuditEntryWriterExtendable
{
	void registerAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion);

	void unregisterAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion);
}
