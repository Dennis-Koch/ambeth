package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;

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
