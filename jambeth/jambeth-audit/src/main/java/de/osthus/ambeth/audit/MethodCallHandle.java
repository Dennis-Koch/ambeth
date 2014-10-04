package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditedService;

public class MethodCallHandle implements IMethodCallHandle
{
	protected final IAuditedService auditedService;

	protected final long start;

	public MethodCallHandle(IAuditedService auditedService, long start)
	{
		this.auditedService = auditedService;
		this.start = start;
	}

	public IAuditedService getAuditEntry()
	{
		return auditedService;
	}

	public long getStart()
	{
		return start;
	}
}
