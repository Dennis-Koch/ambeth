package de.osthus.ambeth.example.audit;

public interface IMyAuditedService {
	boolean myAuditedMethod(String someArg);

	boolean myNotAuditedMethod(String someArg);
}
