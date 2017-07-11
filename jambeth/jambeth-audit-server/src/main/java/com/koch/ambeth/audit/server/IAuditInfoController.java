package com.koch.ambeth.audit.server;

import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.util.state.IStateRollback;

public interface IAuditInfoController {
	void pushAuditReason(String auditReason);

	String popAuditReason();

	String peekAuditReason();

	void pushAuditContext(String auditContext);

	String popAuditContext();

	String peekAuditContext();

	void removeAuditInfo();

	IStateRollback pushClearTextPassword(char[] clearTextPassword, IStateRollback... rollbacks);

	String createAuditedValueOfEntityPrimitive(Object primitiveValueOfEntity);

	IStateRollback pushAuthorizedUser(IUser user, char[] clearTextPassword,
			boolean forceGivenAuthorization, IStateRollback... rollbacks);
}
