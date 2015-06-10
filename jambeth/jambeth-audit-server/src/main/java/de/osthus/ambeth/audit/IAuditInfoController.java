package de.osthus.ambeth.audit;

import de.osthus.ambeth.security.model.IUser;

public interface IAuditInfoController
{
	void pushAuditReason(String auditReason);

	String popAuditReason();

	String peekAuditReason();

	void pushAuditContext(String auditContext);

	String popAuditContext();

	String peekAuditContext();

	void removeAuditInfo();

	IAuditInfoRevert pushClearTextPassword(char[] clearTextPassword);

	String createAuditedValueOfEntityPrimitive(Object primitiveValueOfEntity);

	IAuditInfoRevert setAuthorizedUser(IUser user, char[] clearTextPassword, boolean forceGivenAuthorization);
}