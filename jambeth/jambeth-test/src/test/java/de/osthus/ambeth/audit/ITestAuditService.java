package de.osthus.ambeth.audit;

public interface ITestAuditService
{
	String auditedServiceCall(Integer myArg);

	String notAuditedServiceCall(Integer myArg);

	String auditedServiceCallWithAuditedArgument(Integer myAuditedArg, String myNotAuditedPassword);

	String auditedAnnotatedServiceCall_NoAudit(Integer myArg);
}
