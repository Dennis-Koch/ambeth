package de.osthus.ambeth.audit.model;

public interface IAuditedService
{
	public static final String Arguments = "Arguments";

	public static final String MethodName = "MethodName";

	public static final String Order = "Order";

	public static final String ServiceType = "ServiceType";

	public static final String SpentTime = "SpentTime";

	int getOrder();

	String getServiceType();

	String getMethodName();

	String[] getArguments();

	long getSpentTime();
}
