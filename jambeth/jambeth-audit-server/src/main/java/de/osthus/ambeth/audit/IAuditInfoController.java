package de.osthus.ambeth.audit;

public interface IAuditInfoController
{
	void pushAuditReason(String auditReason);

	String popAuditReason();

	String peekAuditReason();

	void pushAuditContext(String auditContext);

	String popAuditContext();

	String peekAuditContext();

	void removeAuditInfo();
}