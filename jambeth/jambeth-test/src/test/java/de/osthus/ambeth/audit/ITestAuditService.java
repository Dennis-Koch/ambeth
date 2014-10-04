package de.osthus.ambeth.audit;

public interface ITestAuditService
{
	String auditedServiceCall(Integer myArg);

	String notAuditedServiceCall(Integer myArg);
}
