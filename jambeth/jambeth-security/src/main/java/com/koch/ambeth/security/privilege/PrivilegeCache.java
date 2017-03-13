package com.koch.ambeth.security.privilege;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.privilege.PrivilegeProvider.PrivilegeKey;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.util.collections.HashMap;

public class PrivilegeCache implements IPrivilegeCache
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final HashMap<PrivilegeKey, IPrivilege> privilegeCache = new HashMap<PrivilegeKey, IPrivilege>();

	@Override
	public void dispose()
	{
	}
}
