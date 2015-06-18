package de.osthus.ambeth.audit;

import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.util.IRevertDelegate;

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