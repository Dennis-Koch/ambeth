package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.cache.ISingleCacheRunnable;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.transfer.PrivilegeResult;

public class PrivilegeServiceCall implements ISingleCacheRunnable<List<PrivilegeResult>>
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
	public List<PrivilegeResult> run() throws Throwable
	{
		return privilegeService.getPrivilegesIntern(objRefs, securityScopes);
	}
}