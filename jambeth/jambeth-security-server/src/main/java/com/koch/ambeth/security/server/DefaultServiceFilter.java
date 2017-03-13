package com.koch.ambeth.security.server;

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.CallPermission;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IServiceFilter;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class DefaultServiceFilter implements IServiceFilter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public CallPermission checkCallPermissionOnService(Method method, Object[] arguments, SecurityContextType securityContextType,
			IAuthorization authorization, ISecurityScope[] securityScopes)
	{
		if (authorization == null || !authorization.isValid())
		{
			if (SecurityContextType.NOT_REQUIRED.equals(securityContextType))
			{
				return CallPermission.ALLOWED;
			}
			return CallPermission.FORBIDDEN;
		}
		return authorization.getCallPermission(method, securityScopes);
	}
}
