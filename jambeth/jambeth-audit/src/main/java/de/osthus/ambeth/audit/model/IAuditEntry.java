package de.osthus.ambeth.audit.model;

public interface IAuditEntry
{
	String getMethodName();

	void setMethodName(String methodName);

	long getSpentTime();

	void setSpentTime(long spentTime);
}
