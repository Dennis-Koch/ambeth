package com.koch.ambeth.audit.server;

import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.util.state.IStateRollback;

public interface IAuditInfoController {
    IStateRollback pushAuditReason(String auditReason);

    String peekAuditReason();

    IStateRollback pushAuditContext(String auditContext);

    String peekAuditContext();

    void removeAuditInfo();

    IStateRollback pushClearTextPassword(char[] clearTextPassword);

    String createAuditedValueOfEntityPrimitive(Object primitiveValueOfEntity);

    IStateRollback pushAuthorizedUser(IUser user, char[] clearTextPassword, boolean forceGivenAuthorization);
}
