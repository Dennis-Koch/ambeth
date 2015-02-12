package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.transfer.IPrivilegeOfService;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

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
