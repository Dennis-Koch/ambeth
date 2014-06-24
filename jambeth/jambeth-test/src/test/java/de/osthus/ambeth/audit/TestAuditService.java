package de.osthus.ambeth.audit;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;

@AuditMethod(false)
@PersistenceContext
public class TestAuditService implements ITestAuditService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@AuditMethod
	@Override
	public String funnyMethod(Integer myArg)
	{
		return myArg.toString();
	}
}
