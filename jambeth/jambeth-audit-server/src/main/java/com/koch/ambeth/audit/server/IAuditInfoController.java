package com.koch.ambeth.audit.server;

import com.koch.ambeth.ioc.util.IRevertDelegate;
import com.koch.ambeth.security.model.IUser;

public interface IAuditInfoController
{
	void pushAuditReason(String auditReason);

	String popAuditReason();

	String peekAuditReason();

	void pushAuditContext(String auditContext);

	String popAuditContext();

	String peekAuditContext();

	void removeAuditInfo();

	IRevertDelegate pushClearTextPassword(char[] clearTextPassword);

	String createAuditedValueOfEntityPrimitive(Object primitiveValueOfEntity);

	IRevertDelegate setAuthorizedUser(IUser user, char[] clearTextPassword, boolean forceGivenAuthorization);
}