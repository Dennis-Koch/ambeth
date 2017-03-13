package com.koch.ambeth.audit;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.security.audit.model.AuditedArg;

@Audited(false)
@PersistenceContext
public class TestAuditService implements ITestAuditService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Audited
	@Override
	public String auditedServiceCall(Integer myArg)
	{
		return myArg.toString();
	}

	@Audited(false)
	@Override
	public String auditedAnnotatedServiceCall_NoAudit(Integer myArg)
	{
		return myArg.toString();
	}

	@Override
	public String notAuditedServiceCall(Integer myArg)
	{
		return myArg.toString();
	}

	@Audited
	@Override
	public String auditedServiceCallWithAuditedArgument(@AuditedArg Integer myAuditedArg, @AuditedArg(false) String myNotAuditedPassword)
	{
		return myAuditedArg.toString();
	}
}
