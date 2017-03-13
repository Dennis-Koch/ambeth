package com.koch.ambeth.audit.server;

import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;

public class MethodCallHandle implements IMethodCallHandle
{
	protected final CreateOrUpdateContainerBuild auditedService;

	protected final long start;

	public MethodCallHandle(CreateOrUpdateContainerBuild auditedService, long start)
	{
		this.auditedService = auditedService;
		this.start = start;
	}

	public CreateOrUpdateContainerBuild getAuditEntry()
	{
		return auditedService;
	}

	public long getStart()
	{
		return start;
	}
}
