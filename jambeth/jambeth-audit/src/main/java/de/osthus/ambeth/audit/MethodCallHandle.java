package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;

public class MethodCallHandle implements IMethodCallHandle
{
	protected final IAuditEntry auditEntry;

	protected final long start;

	public MethodCallHandle(IAuditEntry auditEntry, long start)
	{
		this.auditEntry = auditEntry;
		this.start = start;
	}

	public IAuditEntry getAuditEntry()
	{
		return auditEntry;
	}

	public long getStart()
	{
		return start;
	}
}
