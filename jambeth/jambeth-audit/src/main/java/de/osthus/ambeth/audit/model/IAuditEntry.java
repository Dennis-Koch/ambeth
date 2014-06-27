package de.osthus.ambeth.audit.model;

import de.osthus.ambeth.security.model.IUser;

public interface IAuditEntry
{
	IUser getUser();

	void setUser(IUser user);

	String getServiceType();

	void setServiceType(String serviceType);

	String getMethodName();

	void setMethodName(String methodName);

	long getSpentTime();

	void setSpentTime(long spentTime);
}
