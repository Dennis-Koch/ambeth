package de.osthus.ambeth.audit.model;

public interface IAuditedService
{
	public static final String MethodName = "MethodName";

	public static final String ServiceType = "ServiceType";

	public static final String SpentTime = "SpentTime";

	int getOrder();

	void setOrder(int order);

	String getServiceType();

	void setServiceType(String serviceType);

	String getMethodName();

	void setMethodName(String methodName);

	long getSpentTime();

	void setSpentTime(long spentTime);
}
