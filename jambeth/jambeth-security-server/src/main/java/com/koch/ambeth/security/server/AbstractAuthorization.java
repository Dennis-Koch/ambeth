package com.koch.ambeth.security.server;

/*-
 * #%L
 * jambeth-security-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.koch.ambeth.security.CallPermission;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IServicePermission;
import com.koch.ambeth.security.PermissionApplyType;
import com.koch.ambeth.security.privilege.model.IPropertyPrivilege;
import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;
import com.koch.ambeth.security.privilege.model.impl.DefaultTypePrivilegeImpl;
import com.koch.ambeth.security.privilege.model.impl.SimpleTypePrivilegeImpl;
import com.koch.ambeth.security.privilege.model.impl.TypePropertyPrivilegeImpl;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.collections.Tuple2KeyEntry;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public abstract class AbstractAuthorization implements IAuthorization {
	private static final IServicePermission[] EMPTY_SERVICE_PERMISSIONS = new IServicePermission[0];

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

	public AbstractAuthorization(HashMap<ISecurityScope, IServicePermission[]> servicePermissions,
			ISecurityScope[] securityScopes,
			Tuple2KeyHashMap<ISecurityScope, String, Boolean> actionPrivileges,
			Tuple2KeyHashMap<ISecurityScope, Class<?>, ITypePrivilege> entityTypePrivileges,
			ITypePrivilege defaultEntityTypePrivilege, IEntityMetaDataProvider entityMetaDataProvider,
			long authorizationTime, IAuthenticationResult authenticationResult) {
		this.servicePermissions = servicePermissions;
		this.securityScopes = securityScopes;
		this.actionPrivileges = actionPrivileges;
		this.entityTypePrivileges = entityTypePrivileges;
		this.defaultEntityTypePrivilege = defaultEntityTypePrivilege;
		this.entityMetaDataProvider = entityMetaDataProvider;
		this.authorizationTime = authorizationTime;
		this.authenticationResult = authenticationResult;

		patternToValueMap = new IdentityHashMap<>(0.5f);
		HashMap<ISecurityScope, ArrayList<Pattern>> scopeToActionPatternsMap = new HashMap<>();
		for (Tuple2KeyEntry<ISecurityScope, String, Boolean> entry : actionPrivileges) {
			String actionName = entry.getKey2();
			if (!actionName.contains("(") && !actionName.contains("*") && !actionName.contains("?")) {
				continue;
			}
			ISecurityScope scope = entry.getKey1();
			Pattern pattern = Pattern.compile(actionName);

			ArrayList<Pattern> actionPatternList = scopeToActionPatternsMap.get(scope);
			if (actionPatternList == null) {
				actionPatternList = new ArrayList<>();
				scopeToActionPatternsMap.put(scope, actionPatternList);
			}
			actionPatternList.add(pattern);
			patternToValueMap.put(pattern, entry.getValue());
		}
		this.scopeToActionPatternsMap = new HashMap<>();
		for (Entry<ISecurityScope, ArrayList<Pattern>> entry : scopeToActionPatternsMap) {
			this.scopeToActionPatternsMap.put(entry.getKey(), entry.getValue().toArray(Pattern.class));
		}
	}

	@Override
	public long getAuthorizationTime() {
		return authorizationTime;
	}

	@Override
	public IAuthenticationResult getAuthenticationResult() {
		return authenticationResult;
	}

	@Override
	public ISecurityScope[] getSecurityScopes() {
		return securityScopes;
	}

	@Override
	public CallPermission getCallPermission(Method serviceOperation,
			ISecurityScope[] securityScopes) {
		String methodSignature = serviceOperation.getDeclaringClass().getName() + "."
				+ serviceOperation.getName();

		CallPermission callPermission = CallPermission.FORBIDDEN;
		for (IServicePermission servicePermissions : getServicePermissions(securityScopes)) {
			for (Pattern pattern : servicePermissions.getPatterns()) {
				if (pattern.matcher(methodSignature).matches()) {
					switch (servicePermissions.getApplyType()) {
						case ALLOW:
							callPermission = CallPermission.ALLOWED;
							break;
						case DENY:
							return CallPermission.FORBIDDEN;
						default:
							throw new IllegalArgumentException(PermissionApplyType.class.getName()
									+ " not supported: " + servicePermissions.getApplyType());
					}
				}
			}
		}
		return callPermission;
	}

	protected IServicePermission[] getServicePermissions(ISecurityScope[] securityScopes) {
		if (securityScopes.length == 0) {
			return EMPTY_SERVICE_PERMISSIONS;
		}
		if (securityScopes.length == 1) {
			IServicePermission[] servicePermissions = this.servicePermissions.get(securityScopes[0]);
			if (servicePermissions == null) {
				return EMPTY_SERVICE_PERMISSIONS;
			}
			return servicePermissions;
		}
		LinkedHashSet<IServicePermission> servicePermissionSet = new LinkedHashSet<>();
		for (int a = 0, size = securityScopes.length; a < size; a++) {
			IServicePermission[] oneServicePermissions = servicePermissions.get(securityScopes[a]);
			if (oneServicePermissions == null) {
				continue;
			}
			servicePermissionSet.addAll(oneServicePermissions);
		}
		return servicePermissionSet.size() == 0 ? EMPTY_SERVICE_PERMISSIONS
				: servicePermissionSet.toArray(IServicePermission.class);
	}

	@Override
	public boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes) {
		if (actionPrivileges == null) {
			return false;
		}
		if (securityScopes.length == 1) {
			Boolean permissionValue = hasActionPermissionIntern(actionPermissionName, securityScopes[0]);
			return Boolean.TRUE.equals(permissionValue);
		}
		for (int a = 0, size = securityScopes.length; a < size; a++) {
			Boolean permissionValue = hasActionPermissionIntern(actionPermissionName, securityScopes[a]);
			if (Boolean.TRUE.equals(permissionValue)) {
				return true;
			}
		}
		return false;
	}

	protected Boolean hasActionPermissionIntern(String actionPermissionName,
			ISecurityScope securityScope) {
		synchronized (actionPrivileges) {
			Boolean value = actionPrivileges.get(securityScope, actionPermissionName);
			if (value != null) {
				return value;
			}
			Pattern[] actionPatterns = scopeToActionPatternsMap.get(securityScope);
			if (actionPatterns != null) {
				for (Pattern actionPattern : actionPatterns) {
					if (!actionPattern.matcher(actionPermissionName).matches()) {
						continue;
					}
					Boolean valueOfPattern = patternToValueMap.get(actionPattern);
					if (Boolean.TRUE.equals(valueOfPattern)) {
						value = Boolean.TRUE;
						break;
					}
				}
			}
			if (value == null) {
				value = Boolean.FALSE;
			}
			actionPrivileges.put(securityScope, actionPermissionName, value);
			return value;
		}
	}

	@Override
	public ITypePrivilege getEntityTypePrivilege(Class<?> entityType,
			ISecurityScope[] securityScopes) {
		if (entityTypePrivileges == null) {
			return defaultEntityTypePrivilege;
		}
		synchronized (entityTypePrivileges) {
			if (securityScopes.length == 1) {
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
			for (int a = 0, size = securityScopes.length; a < size; a++) {
				ITypePrivilege typePrivilege = entityTypePrivileges.get(securityScopes[a], entityType);
				if (typePrivilege == null) {
					continue;
				}
				read = unionFlags(read, typePrivilege.isReadAllowed());
				create = unionFlags(create, typePrivilege.isCreateAllowed());
				update = unionFlags(update, typePrivilege.isUpdateAllowed());
				delete = unionFlags(delete, typePrivilege.isDeleteAllowed());
				execute = unionFlags(execute, typePrivilege.isExecuteAllowed());

				ITypePropertyPrivilege defaultPropertyPrivilegeIfValid = typePrivilege
						.getDefaultPropertyPrivilegeIfValid();
				if (defaultPropertyPrivilegeIfValid != null) {
					if (fastPropertyHandling) {
						defaultPropertyPrivilege = unionPropertyPrivileges(defaultPropertyPrivilege,
								defaultPropertyPrivilegeIfValid);
						continue;
					}
					// fastPropertyHandling not possible any more because of preceeding custom privileges
					// so we union this default privilege with each existing specific property privilege
					unionPropertyPrivileges(primitivePropertyPrivileges, defaultPropertyPrivilegeIfValid);
					unionPropertyPrivileges(relationPropertyPrivileges, defaultPropertyPrivilegeIfValid);
					continue;
				}
				if (fastPropertyHandling) {
					// fastPropertyHandling no longer possible: we have to define specific property privileges
					fastPropertyHandling = false;
					if (defaultPropertyPrivilege != null) {
						primitivePropertyPrivileges = new ITypePropertyPrivilege[primitiveCount];
						relationPropertyPrivileges = new ITypePropertyPrivilege[relationCount];
						Arrays.fill(primitivePropertyPrivileges, defaultPropertyPrivilege);
						Arrays.fill(relationPropertyPrivileges, defaultPropertyPrivilege);
						defaultPropertyPrivilege = null;
					}
					// no we are ready to union all specific property privileges
				}
				for (int b = primitivePropertyPrivileges.length; b-- > 0;) {
					primitivePropertyPrivileges[b] = unionPropertyPrivileges(primitivePropertyPrivileges[b],
							typePrivilege.getPrimitivePropertyPrivilege(b));
				}
				for (int b = relationPropertyPrivileges.length; b-- > 0;) {
					relationPropertyPrivileges[b] = unionPropertyPrivileges(relationPropertyPrivileges[b],
							typePrivilege.getRelationPropertyPrivilege(b));
				}
			}
			if (fastPropertyHandling) {
				return new SimpleTypePrivilegeImpl(create, read, update, delete, execute,
						defaultPropertyPrivilege);
			}
			// the default propertyPrivilege is aligned with the entityType privilege
			defaultPropertyPrivilege = TypePropertyPrivilegeImpl.create(create, read, update, delete);
			for (int a = primitivePropertyPrivileges.length; a-- > 0;) {
				if (primitivePropertyPrivileges[a] == null) {
					primitivePropertyPrivileges[a] = defaultPropertyPrivilege;
				}
			}
			for (int a = relationPropertyPrivileges.length; a-- > 0;) {
				if (relationPropertyPrivileges[a] == null) {
					relationPropertyPrivileges[a] = defaultPropertyPrivilege;
				}
			}
			return new DefaultTypePrivilegeImpl(create, read, update, delete, execute,
					primitivePropertyPrivileges, relationPropertyPrivileges);
		}
	}

	public static ITypePropertyPrivilege unionPropertyPrivileges(ITypePropertyPrivilege left,
			ITypePropertyPrivilege right) {
		if (left == null) {
			if (right != null) {
				return right;
			}
			throw new IllegalArgumentException(
					"At least one instance must be a valid " + IPropertyPrivilege.class.getSimpleName());
		}
		else if (right == null) {
			return left;
		}
		return TypePropertyPrivilegeImpl.create(
				unionFlags(left.isCreateAllowed(), right.isCreateAllowed()),
				unionFlags(left.isReadAllowed(), right.isReadAllowed()),
				unionFlags(left.isUpdateAllowed(), right.isUpdateAllowed()),
				unionFlags(left.isDeleteAllowed(), right.isDeleteAllowed()));
	}

	public static Boolean unionFlags(Boolean left, Boolean right) {
		if (left == null) {
			if (right != null) {
				return right;
			}
			return null;
		}
		else if (right == null) {
			return left;
		}
		return Boolean.valueOf(left.booleanValue() || right.booleanValue());
	}

	public static void unionPropertyPrivileges(ITypePropertyPrivilege[] lefts,
			ITypePropertyPrivilege right) {
		for (int a = lefts.length; a-- > 0;) {
			lefts[a] = unionPropertyPrivileges(lefts[a], right);
		}
	}
}
