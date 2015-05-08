package de.osthus.ambeth.audit.model;


@Audited(false)
public interface IAuditedService
{
	public static final String Arguments = "Arguments";

	public static final String Entry = "Entry";

	public static final String MethodName = "MethodName";

	public static final String Order = "Order";

	public static final String ServiceType = "ServiceType";

	public static final String SpentTime = "SpentTime";

	String[] getArguments();

	IAuditEntry getEntry();

	String getMethodName();

	int getOrder();

	String getServiceType();

	long getSpentTime();
}
