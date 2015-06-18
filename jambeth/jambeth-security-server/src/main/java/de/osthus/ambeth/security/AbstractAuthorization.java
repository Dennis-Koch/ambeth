package de.osthus.ambeth.security;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.collections.Tuple2KeyEntry;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.DefaultTypePrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.SimpleTypePrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.TypePropertyPrivilegeImpl;

public abstract class AbstractAuthorization implements IAuthorization
{
	private final IEntityMetaDataProvider entityMetaDataProvider;

	private final ISecurityScope[] securityScopes;

	private final HashMap<ISecurityScope, IServicePermission[]> servicePermissions;

	private final Tuple2KeyHashMap<ISecurityScope, String, Boolean> actionPrivileges;

	private final Tuple2KeyHashMap<ISecurityScope, Class<?>, ITypePrivilege> entityTypePrivileges;

	private final HashMap<ISecurityScope, Pattern[]> scopeToActionPatternsMap;

	private final IdentityHashMap<Pattern, Boolean> patternToValueMap;

	private final ITypePrivilege defaultEntityTypePrivilege;

	private final long authorizationTime;

	private final IAuthenticationResult authenticationResult;

	public AbstractAuthorization(HashMap<ISecurityScope, IServicePermission[]> servicePermissions, ISecurityScope[] securityScopes,
			Tuple2KeyHashMap<ISecurityScope, String, Boolean> actionPrivileges,
			Tuple2KeyHashMap<ISecurityScope, Class<?>, ITypePrivilege> entityTypePrivileges, ITypePrivilege defaultEntityTypePrivilege,
			IEntityMetaDataProvider entityMetaDataProvider, long authorizationTime, IAuthenticationResult authenticationResult)
	{
		this.servicePermissions = servicePermissions;
		this.securityScopes = securityScopes;
		this.actionPrivileges = actionPrivileges;
		this.entityTypePrivileges = entityTypePrivileges;
		this.defaultEntityTypePrivilege = defaultEntityTypePrivilege;
		this.entityMetaDataProvider = entityMetaDataProvider;
		this.authorizationTime = authorizationTime;
		this.authenticationResult = authenticationResult;

		patternToValueMap = new IdentityHashMap<Pattern, Boolean>(0.5f);
		HashMap<ISecurityScope, ArrayList<Pattern>> scopeToActionPatternsMap = new HashMap<ISecurityScope, ArrayList<Pattern>>();
		for (Tuple2KeyEntry<ISecurityScope, String, Boolean> entry : actionPrivileges)
		{
			String actionName = entry.getKey2();
			if (!actionName.contains("(") && !actionName.contains("*") && !actionName.contains("?"))
			{
				continue;
			}
			ISecurityScope scope = entry.getKey1();
			Pattern pattern = Pattern.compile(actionName);

			ArrayList<Pattern> actionPatternList = scopeToActionPatternsMap.get(scope);
			if (actionPatternList == null)
			{
				actionPatternList = new ArrayList<Pattern>();
				scopeToActionPatternsMap.put(scope, actionPatternList);
			}
			actionPatternList.add(pattern);
			patternToValueMap.put(pattern, entry.getValue());
		}
		this.scopeToActionPatternsMap = new HashMap<ISecurityScope, Pattern[]>();
		for (Entry<ISecurityScope, ArrayList<Pattern>> entry : scopeToActionPatternsMap)
		{
			this.scopeToActionPatternsMap.put(entry.getKey(), entry.getValue().toArray(Pattern.class));
		}
	}

	@Override
	public long getAuthorizationTime()
	{
		return authorizationTime;
	}

	@Override
	public IAuthenticationResult getAuthenticationResult()
	{
		return authenticationResult;
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
		if (actionPrivileges == null)
		{
			return false;
		}
		if (securityScopes.length == 1)
		{
			Boolean permissionValue = hasActionPermissionIntern(actionPermissionName, securityScopes[0]);
			return Boolean.TRUE.equals(permissionValue);
		}
		for (int a = 0, size = securityScopes.length; a < size; a++)
		{
			Boolean permissionValue = hasActionPermissionIntern(actionPermissionName, securityScopes[a]);
			if (Boolean.TRUE.equals(permissionValue))
			{
				return true;
			}
		}
		return false;
	}

	protected Boolean hasActionPermissionIntern(String actionPermissionName, ISecurityScope securityScope)
	{
		Boolean value = actionPrivileges.get(securityScope, actionPermissionName);
		if (value != null)
		{
			return value;
		}
		Pattern[] actionPatterns = scopeToActionPatternsMap.get(securityScope);
		if (actionPatterns != null)
		{
			for (Pattern actionPattern : actionPatterns)
			{
				if (!actionPattern.matcher(actionPermissionName).matches())
				{
					continue;
				}
				Boolean valueOfPattern = patternToValueMap.get(actionPattern);
				if (Boolean.TRUE.equals(valueOfPattern))
				{
					value = Boolean.TRUE;
					break;
				}
			}
		}
		if (value == null)
		{
			value = Boolean.FALSE;
		}
		actionPrivileges.put(securityScope, actionPermissionName, value);
		return value;
	}

	@Override
	public ITypePrivilege getEntityTypePrivilege(Class<?> entityType, ISecurityScope[] securityScopes)
	{
		if (entityTypePrivileges == null)
		{
			return defaultEntityTypePrivilege;
		}
		if (securityScopes.length == 1)
		{
			ITypePrivilege typePrivilege = entityTypePrivileges.get(securityScopes[0], entityType);
			return typePrivilege != null ? typePrivilege : defaultEntityTypePrivilege;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		int primitiveCount = metaData.getPrimitiveMembers().length;
		int relationCount = metaData.getRelationMembers().length;
		ITypePropertyPrivilege defaultPropertyPrivilege = null;
		ITypePropertyPrivilege[] primitivePropertyPrivileges = null;
		ITypePropertyPrivilege[] relationPropertyPrivileges = null;
		boolean fastPropertyHandling = true;
		Boolean read = false, create = false, update = false, delete = false, execute = false;
		for (int a = 0, size = securityScopes.length; a < size; a++)
		{
			ITypePrivilege typePrivilege = entityTypePrivileges.get(securityScopes[a], entityType);
			if (typePrivilege == null)
			{
				continue;
			}
			read = unionFlags(read, typePrivilege.isReadAllowed());
			create = unionFlags(create, typePrivilege.isCreateAllowed());
			update = unionFlags(update, typePrivilege.isUpdateAllowed());
			delete = unionFlags(delete, typePrivilege.isDeleteAllowed());
			execute = unionFlags(execute, typePrivilege.isExecuteAllowed());

			ITypePropertyPrivilege defaultPropertyPrivilegeIfValid = typePrivilege.getDefaultPropertyPrivilegeIfValid();
			if (defaultPropertyPrivilegeIfValid != null)
			{
				if (fastPropertyHandling)
				{
					defaultPropertyPrivilege = unionPropertyPrivileges(defaultPropertyPrivilege, defaultPropertyPrivilegeIfValid);
					continue;
				}
				// fastPropertyHandling not possible any more because of preceeding custom privileges
				// so we union this default privilege with each existing specific property privilege
				unionPropertyPrivileges(primitivePropertyPrivileges, defaultPropertyPrivilegeIfValid);
				unionPropertyPrivileges(relationPropertyPrivileges, defaultPropertyPrivilegeIfValid);
				continue;
			}
			if (fastPropertyHandling)
			{
				// fastPropertyHandling no longer possible: we have to define specific property privileges
				fastPropertyHandling = false;
				if (defaultPropertyPrivilege != null)
				{
					primitivePropertyPrivileges = new ITypePropertyPrivilege[primitiveCount];
					relationPropertyPrivileges = new ITypePropertyPrivilege[relationCount];
					Arrays.fill(primitivePropertyPrivileges, defaultPropertyPrivilege);
					Arrays.fill(relationPropertyPrivileges, defaultPropertyPrivilege);
					defaultPropertyPrivilege = null;
				}
				// no we are ready to union all specific property privileges
			}
			for (int b = primitivePropertyPrivileges.length; b-- > 0;)
			{
				primitivePropertyPrivileges[b] = unionPropertyPrivileges(primitivePropertyPrivileges[b], typePrivilege.getPrimitivePropertyPrivilege(b));
			}
			for (int b = relationPropertyPrivileges.length; b-- > 0;)
			{
				relationPropertyPrivileges[b] = unionPropertyPrivileges(relationPropertyPrivileges[b], typePrivilege.getRelationPropertyPrivilege(b));
			}
		}
		if (fastPropertyHandling)
		{
			return new SimpleTypePrivilegeImpl(create, read, update, delete, execute, defaultPropertyPrivilege);
		}
		// the default propertyPrivilege is aligned with the entityType privilege
		defaultPropertyPrivilege = TypePropertyPrivilegeImpl.create(create, read, update, delete);
		for (int a = primitivePropertyPrivileges.length; a-- > 0;)
		{
			if (primitivePropertyPrivileges[a] == null)
			{
				primitivePropertyPrivileges[a] = defaultPropertyPrivilege;
			}
		}
		for (int a = relationPropertyPrivileges.length; a-- > 0;)
		{
			if (relationPropertyPrivileges[a] == null)
			{
				relationPropertyPrivileges[a] = defaultPropertyPrivilege;
			}
		}
		return new DefaultTypePrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
	}

	public static ITypePropertyPrivilege unionPropertyPrivileges(ITypePropertyPrivilege left, ITypePropertyPrivilege right)
	{
		if (left == null)
		{
			if (right != null)
			{
				return right;
			}
			throw new IllegalArgumentException("At least one instance must be a valid " + IPropertyPrivilege.class.getSimpleName());
		}
		else if (right == null)
		{
			return left;
		}
		return TypePropertyPrivilegeImpl.create(unionFlags(left.isCreateAllowed(), right.isCreateAllowed()),
				unionFlags(left.isReadAllowed(), right.isReadAllowed()), unionFlags(left.isUpdateAllowed(), right.isUpdateAllowed()),
				unionFlags(left.isDeleteAllowed(), right.isDeleteAllowed()));
	}

	public static Boolean unionFlags(Boolean left, Boolean right)
	{
		if (left == null)
		{
			if (right != null)
			{
				return right;
			}
			return null;
		}
		else if (right == null)
		{
			return left;
		}
		return Boolean.valueOf(left.booleanValue() || right.booleanValue());
	}

	public static void unionPropertyPrivileges(ITypePropertyPrivilege[] lefts, ITypePropertyPrivilege right)
	{
		for (int a = lefts.length; a-- > 0;)
		{
			lefts[a] = unionPropertyPrivileges(lefts[a], right);
		}
	}
}
