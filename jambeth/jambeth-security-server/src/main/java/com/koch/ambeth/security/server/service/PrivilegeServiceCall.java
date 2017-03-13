package com.koch.ambeth.security.server.service;

import java.util.List;

import com.koch.ambeth.security.privilege.transfer.IPrivilegeOfService;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class PrivilegeServiceCall implements IResultingBackgroundWorkerDelegate<List<IPrivilegeOfService>>
{
	private final IObjRef[] objRefs;

	private final ISecurityScope[] securityScopes;

	private final PrivilegeService privilegeService;

	public PrivilegeServiceCall(IObjRef[] objRefs, ISecurityScope[] securityScopes, PrivilegeService privilegeService)
	{
		this.objRefs = objRefs;
		this.securityScopes = securityScopes;
		this.privilegeService = privilegeService;
	}

	@Override
	public List<IPrivilegeOfService> invoke() throws Throwable
	{
		return privilegeService.getPrivilegesIntern(objRefs, securityScopes);
	}
}
