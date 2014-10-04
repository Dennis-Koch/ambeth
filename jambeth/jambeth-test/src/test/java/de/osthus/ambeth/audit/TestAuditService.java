package de.osthus.ambeth.audit;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;

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

	@Override
	public String notAuditedServiceCall(Integer myArg)
	{
		return myArg.toString();
	}
}
