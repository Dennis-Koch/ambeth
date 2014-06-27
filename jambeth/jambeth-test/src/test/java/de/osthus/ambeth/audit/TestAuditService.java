package de.osthus.ambeth.audit;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;

@AuditAccess(false)
@PersistenceContext
public class TestAuditService implements ITestAuditService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@AuditAccess
	@Override
	public String funnyMethod(Integer myArg)
	{
		return myArg.toString();
	}
}
