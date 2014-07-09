package de.osthus.ambeth.privilege;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.privilege.PrivilegeProvider.PrivilegeKey;
import de.osthus.ambeth.privilege.model.IPrivilege;

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
