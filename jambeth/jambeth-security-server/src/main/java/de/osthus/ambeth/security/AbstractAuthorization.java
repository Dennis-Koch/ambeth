package de.osthus.ambeth.security;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.model.ISecurityScope;

public abstract class AbstractAuthorization implements IAuthorization
{
	private final ISecurityScope[] securityScopes;

	private final HashMap<ISecurityScope, IServicePermission[]> servicePermissions;

	private final HashMap<ISecurityScope, LinkedHashMap<String, Boolean>> actionPermissions;

	public AbstractAuthorization(HashMap<ISecurityScope, IServicePermission[]> servicePermissions, ISecurityScope[] securityScopes,
			HashMap<ISecurityScope, LinkedHashMap<String, Boolean>> actionPermissions)
	{
		this.servicePermissions = servicePermissions;
		this.securityScopes = securityScopes;
		this.actionPermissions = actionPermissions;
	}

	@Override
	public ISecurityScope[] getSecurityScopes()
	{
		return securityScopes;
	}

	@Override
	public CallPermission getCallPermission(Method serviceOperation, ISecurityScope[] securityScopes)
	{
		String methodSignature = serviceOperation.getDeclaringClass().getName() + "." + serviceOperation.getName();

		CallPermission callPermission = CallPermission.FORBIDDEN;
		for (IServicePermission servicePermissions : getServicePermissions(securityScopes))
		{
			for (Pattern pattern : servicePermissions.getPatterns())
			{
				if (pattern.matcher(methodSignature).matches())
				{
					switch (servicePermissions.getApplyType())
					{
						case ALLOW:
							callPermission = CallPermission.ALLOWED;
							break;
						case DENY:
							return CallPermission.FORBIDDEN;
						default:
							throw new IllegalArgumentException(PermissionApplyType.class.getName() + " not supported: " + servicePermissions.getApplyType());
					}
				}
			}
		}
		return callPermission;
	}

	protected IServicePermission[] getServicePermissions(ISecurityScope[] securityScopes)
	{
		if (securityScopes.length == 1)
		{
			return servicePermissions.get(securityScopes[0]);
		}
		LinkedHashSet<IServicePermission> servicePermissionSet = new LinkedHashSet<IServicePermission>();
		for (int a = 0, size = securityScopes.length; a < size; a++)
		{
			IServicePermission[] oneServicePermissions = servicePermissions.get(securityScopes[a]);
			servicePermissionSet.addAll(oneServicePermissions);
		}
		return servicePermissionSet.toArray(IServicePermission.class);
	}

	@Override
	public boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes)
	{
		if (actionPermissions == null)
		{
			return false;
		}
		if (securityScopes.length == 1)
		{
			Boolean permissionValue = actionPermissions.get(securityScopes[0]).get(actionPermissionName);
			return permissionValue != null && permissionValue.booleanValue();
		}
		for (int a = 0, size = securityScopes.length; a < size; a++)
		{
			Boolean permissionValue = actionPermissions.get(securityScopes[a]).get(actionPermissionName);
			if (permissionValue != null && permissionValue.booleanValue())
			{
				return true;
			}
		}
		return false;
	}
}
