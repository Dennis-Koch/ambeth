package de.osthus.ambeth.security;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.EmptyMap;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.PrivilegeImpl;

public abstract class AbstractAuthorization implements IAuthorization
{
	private final ISecurityScope[] securityScopes;

	private final HashMap<ISecurityScope, IServicePermission[]> servicePermissions;

	private final Tuple2KeyHashMap<ISecurityScope, String, Boolean> actionPermissions;

	private final Tuple2KeyHashMap<ISecurityScope, Class<?>, IPrivilege> entityPermissions;

	private final IPrivilege defaultEntityPermission;

	public AbstractAuthorization(HashMap<ISecurityScope, IServicePermission[]> servicePermissions, ISecurityScope[] securityScopes,
			Tuple2KeyHashMap<ISecurityScope, String, Boolean> actionPermissions, Tuple2KeyHashMap<ISecurityScope, Class<?>, IPrivilege> entityPermissions,
			IPrivilege defaultEntityPermission)
	{
		this.servicePermissions = servicePermissions;
		this.securityScopes = securityScopes;
		this.actionPermissions = actionPermissions;
		this.entityPermissions = entityPermissions;
		this.defaultEntityPermission = defaultEntityPermission;
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
			Boolean permissionValue = actionPermissions.get(securityScopes[0], actionPermissionName);
			return Boolean.TRUE.equals(permissionValue);
		}
		for (int a = 0, size = securityScopes.length; a < size; a++)
		{
			Boolean permissionValue = actionPermissions.get(securityScopes[a], actionPermissionName);
			if (Boolean.TRUE.equals(permissionValue))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IPrivilege getEntityTypePrivilege(Class<?> entityType, ISecurityScope[] securityScopes)
	{
		if (entityPermissions == null)
		{
			return defaultEntityPermission;
		}
		if (securityScopes.length == 1)
		{
			IPrivilege privilegeItem = entityPermissions.get(securityScopes[0], entityType);
			return privilegeItem != null ? privilegeItem : defaultEntityPermission;
		}
		IMap<String, IPropertyPrivilege> propertyPrivilegeMap = null;
		boolean read = false, create = false, update = false, delete = false, execute = false;
		for (int a = 0, size = securityScopes.length; a < size; a++)
		{
			IPrivilege privilegeItem = entityPermissions.get(securityScopes[a], entityType);
			if (privilegeItem == null)
			{
				continue;
			}
			read |= privilegeItem.isReadAllowed();
			create |= privilegeItem.isCreateAllowed();
			update |= privilegeItem.isUpdateAllowed();
			delete |= privilegeItem.isDeleteAllowed();
			execute |= privilegeItem.isExecutionAllowed();
			if (privilegeItem.getConfiguredPropertyNames().length > 0)
			{
				throw new UnsupportedOperationException("It is not yet supported to work in multiple " + ISecurityScope.class.getName()
						+ " at once when specific property permissions are configured");
			}
		}
		if (propertyPrivilegeMap == null)
		{
			propertyPrivilegeMap = EmptyMap.emptyMap();
		}
		return new PrivilegeImpl(read, create, update, delete, execute, propertyPrivilegeMap, propertyPrivilegeMap.keySet().toArray(String.class));
	}
}
