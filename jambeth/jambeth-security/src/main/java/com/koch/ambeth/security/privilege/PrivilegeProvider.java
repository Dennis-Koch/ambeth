package com.koch.ambeth.security.privilege;

/*-
 * #%L
 * jambeth-security
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.datachange.IDataChangeListener;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationProcess;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.events.ClearAllCachedPrivilegesEvent;
import com.koch.ambeth.security.privilege.factory.IEntityPrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.IEntityTypePrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPrivilegeResult;
import com.koch.ambeth.security.privilege.model.IPropertyPrivilege;
import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePrivilegeResult;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;
import com.koch.ambeth.security.privilege.model.impl.DenyAllPrivilege;
import com.koch.ambeth.security.privilege.model.impl.PrivilegeResult;
import com.koch.ambeth.security.privilege.model.impl.PropertyPrivilegeImpl;
import com.koch.ambeth.security.privilege.model.impl.SimplePrivilegeImpl;
import com.koch.ambeth.security.privilege.model.impl.SimpleTypePrivilegeImpl;
import com.koch.ambeth.security.privilege.model.impl.SkipAllTypePrivilege;
import com.koch.ambeth.security.privilege.model.impl.TypePrivilegeResult;
import com.koch.ambeth.security.privilege.model.impl.TypePropertyPrivilegeImpl;
import com.koch.ambeth.security.privilege.transfer.IPrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.IPropertyPrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.ITypePrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.ITypePropertyPrivilegeOfService;
import com.koch.ambeth.security.service.IPrivilegeService;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.IInterningFeature;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.Tuple3KeyHashMap;

public class PrivilegeProvider
		implements IPrivilegeProviderIntern, IInitializingBean, IDataChangeListener {

	public static final String HANDLE_CLEAR_ALL_CACHES = "handleClearAllCaches";

	public static final String HANDLE_CLEAR_ALL_PRIVILEGES = "handleClearAllPrivileges";

	public static class PrivilegeKey {
		public Class<?> entityType;

		public Object id;

		public byte idIndex;

		public String securityScope;

		public String userSID;

		public PrivilegeKey() {
			// Intended blank
		}

		public PrivilegeKey(Class<?> entityType, byte IdIndex, Object id, String userSID) {
			this.entityType = entityType;
			idIndex = IdIndex;
			this.id = id;
			this.userSID = userSID;
		}

		@Override
		public int hashCode() {
			return getClass().hashCode() ^ entityType.hashCode() ^ id.hashCode() ^ hashCode(userSID)
					^ hashCode(securityScope);
		}

		protected int hashCode(Object obj) {
			if (obj == null) {
				return 1;
			}
			return obj.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof PrivilegeKey)) {
				return false;
			}
			PrivilegeKey other = (PrivilegeKey) obj;
			return Objects.equals(id, other.id) && Objects.equals(entityType, other.entityType)
					&& idIndex == other.idIndex && Objects.equals(userSID, other.userSID)
					&& Objects.equals(securityScope, other.securityScope);
		}

		@Override
		public String toString() {
			return "PrivilegeKey: " + entityType.getName() + "(" + idIndex + "," + id
					+ ") SecurityScope: '" + securityScope + "',SID:" + userSID;
		}
	}

	@LogInstance
	protected ILogger log;

	@Autowired(optional = true)
	protected IAuthorizationProcess authorizationProcess;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEntityPrivilegeFactoryProvider entityPrivilegeFactoryProvider;

	@Autowired
	protected IEntityTypePrivilegeFactoryProvider entityTypePrivilegeFactoryProvider;

	@Autowired
	protected IInterningFeature interningFeature;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired(optional = true)
	protected IPrivilegeService privilegeService;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	protected final Lock writeLock = new ReentrantLock();

	protected final LinkedHashMap<PrivilegeKey, IPrivilege> privilegeCache = new LinkedHashMap<>();

	protected final Tuple3KeyHashMap<Class<?>, String, String, ITypePrivilege> entityTypePrivilegeCache =
			new Tuple3KeyHashMap<>();

	@Override
	public void afterPropertiesSet() {
		if (privilegeService == null && log.isDebugEnabled()) {
			log.debug("Privilege Service could not be resolved - Privilege functionality deactivated");
		}
	}

	@Override
	public IPrivilegeCache createPrivilegeCache() {
		return beanContext.registerBean(PrivilegeCache.class).finish();
	}

	@Override
	public IPrivilege getPrivilege(Object entity) {
		return getPrivilege(entity, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IPrivilege getPrivilege(Object entity, ISecurityScope[] securityScopes) {
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(entity, null);
		IPrivilegeResult result = getPrivileges(objRefs, securityScopes);
		return result.getPrivileges()[0];
	}

	@Override
	public IPrivilege getPrivilegeByObjRef(IObjRef objRef) {
		return getPrivilegeByObjRef(objRef, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IPrivilege getPrivilegeByObjRef(ObjRef objRef, IPrivilegeCache privilegeCache) {
		return getPrivilegeByObjRef(objRef);
	}

	@Override
	public IPrivilege getPrivilegeByObjRef(IObjRef objRef, ISecurityScope[] securityScopes) {
		IPrivilegeResult result = getPrivilegesByObjRef(
				new ArrayList<IObjRef>(new IObjRef[] {objRef}), securityScopes);
		return result.getPrivileges()[0];
	}

	@Override
	public IPrivilegeResult getPrivileges(List<?> entities) {
		return getPrivileges(entities, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IPrivilegeResult getPrivileges(List<?> entities, ISecurityScope[] securityScopes) {
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(entities, null);
		return getPrivilegesByObjRef(objRefs, securityScopes);
	}

	@Override
	public IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs) {
		return getPrivilegesByObjRef(objRefs, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs,
			IPrivilegeCache privilegeCache) {
		return getPrivilegesByObjRef(objRefs);
	}

	@Override
	public IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs,
			ISecurityScope[] securityScopes) {
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		if (authorization == null) {
			if (authorizationProcess != null) {
				authorizationProcess.tryAuthorization();
				context = securityContextHolder.getContext();
				authorization = context != null ? context.getAuthorization() : null;
			}
		}
		if (securityScopes.length == 0) {
			throw new IllegalArgumentException(
					"No " + ISecurityScope.class.getSimpleName() + " provided to check privileges against");
		}
		ArrayList<IObjRef> missingObjRefs = new ArrayList<>();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			IPrivilegeResult result = createResult(objRefs, securityScopes, missingObjRefs, authorization,
					null);
			if (missingObjRefs.isEmpty()) {
				return result;
			}
		}
		finally {
			writeLock.unlock();
		}
		if (privilegeService == null) {
			throw new SecurityException("No bean of type " + IPrivilegeService.class.getName()
					+ " could be injected. Privilege functionality is deactivated. The current operation is not supported");
		}
		String userSID = authorization != null ? authorization.getSID() : null;
		List<IPrivilegeOfService> privilegeResults = privilegeService
				.getPrivileges(missingObjRefs.toArray(IObjRef.class), securityScopes);
		writeLock.lock();
		try {
			HashMap<PrivilegeKey, IPrivilege> privilegeResultOfNewEntities = null;
			for (int a = 0, size = privilegeResults.size(); a < size; a++) {
				IPrivilegeOfService privilegeResult = privilegeResults.get(a);
				IObjRef reference = privilegeResult.getReference();

				PrivilegeKey privilegeKey = new PrivilegeKey(reference.getRealType(),
						reference.getIdNameIndex(), reference.getId(), userSID);
				boolean useCache = true;
				if (privilegeKey.id == null) {
					useCache = false;
					privilegeKey.id = reference;
				}
				privilegeKey.securityScope = interningFeature
						.intern(privilegeResult.getSecurityScope().getName());

				IPrivilege privilege = createPrivilegeFromServiceResult(reference, privilegeResult);
				if (useCache) {
					privilegeCache.put(privilegeKey, privilege);
				}
				else {
					if (privilegeResultOfNewEntities == null) {
						privilegeResultOfNewEntities = new HashMap<>();
					}
					privilegeResultOfNewEntities.put(privilegeKey, privilege);
				}
			}
			return createResult(objRefs, securityScopes, null, authorization,
					privilegeResultOfNewEntities);
		}
		finally {
			writeLock.unlock();
		}
	}

	protected IPrivilege createPrivilegeFromServiceResult(IObjRef objRef,
			IPrivilegeOfService privilegeOfService) {
		IPropertyPrivilegeOfService[] propertyPrivilegesOfService = privilegeOfService
				.getPropertyPrivileges();

		if (propertyPrivilegesOfService == null || propertyPrivilegesOfService.length == 0) {
			return SimplePrivilegeImpl.createFrom(privilegeOfService);
		}
		String[] propertyPrivilegeNames = privilegeOfService.getPropertyPrivilegeNames();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
		IPropertyPrivilege[] primitivePropertyPrivileges = new IPropertyPrivilege[metaData
				.getPrimitiveMembers().length];
		IPropertyPrivilege[] relationPropertyPrivileges = new IPropertyPrivilege[metaData
				.getRelationMembers().length];
		IPropertyPrivilege defaultPropertyPrivilege = PropertyPrivilegeImpl
				.createFrom(privilegeOfService);
		Arrays.fill(primitivePropertyPrivileges, defaultPropertyPrivilege);
		Arrays.fill(relationPropertyPrivileges, defaultPropertyPrivilege);
		for (int b = propertyPrivilegesOfService.length; b-- > 0;) {
			IPropertyPrivilegeOfService propertyPrivilegeOfService = propertyPrivilegesOfService[b];
			String propertyName = interningFeature.intern(propertyPrivilegeNames[b]);
			IPropertyPrivilege propertyPrivilege = PropertyPrivilegeImpl.create(
					propertyPrivilegeOfService.isCreateAllowed(), propertyPrivilegeOfService.isReadAllowed(),
					propertyPrivilegeOfService.isUpdateAllowed(),
					propertyPrivilegeOfService.isDeleteAllowed());
			if (metaData.isRelationMember(propertyName)) {
				relationPropertyPrivileges[metaData
						.getIndexByRelationName(propertyName)] = propertyPrivilege;
			}
			if (metaData.isPrimitiveMember(propertyName)) {
				primitivePropertyPrivileges[metaData
						.getIndexByPrimitiveName(propertyName)] = propertyPrivilege;
			}
		}
		return entityPrivilegeFactoryProvider
				.getEntityPrivilegeFactory(metaData.getEntityType(), privilegeOfService.isCreateAllowed(),
						privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(),
						privilegeOfService.isDeleteAllowed(), privilegeOfService.isExecuteAllowed())
				.createPrivilege(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(),
						privilegeOfService.isUpdateAllowed(), privilegeOfService.isDeleteAllowed(),
						privilegeOfService.isExecuteAllowed(), primitivePropertyPrivileges,
						relationPropertyPrivileges);
	}

	protected ITypePrivilege createTypePrivilegeFromServiceResult(Class<?> entityType,
			ITypePrivilegeOfService privilegeOfService) {
		ITypePropertyPrivilegeOfService[] propertyPrivilegesOfService = privilegeOfService
				.getPropertyPrivileges();

		ITypePropertyPrivilege defaultPropertyPrivilege = TypePropertyPrivilegeImpl
				.createFrom(privilegeOfService);
		if (propertyPrivilegesOfService == null || propertyPrivilegesOfService.length == 0) {
			return new SimpleTypePrivilegeImpl(privilegeOfService.isCreateAllowed(),
					privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(),
					privilegeOfService.isDeleteAllowed(), privilegeOfService.isExecuteAllowed(),
					defaultPropertyPrivilege);
		}
		String[] propertyPrivilegeNames = privilegeOfService.getPropertyPrivilegeNames();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		ITypePropertyPrivilege[] primitivePropertyPrivileges = new ITypePropertyPrivilege[metaData
				.getPrimitiveMembers().length];
		ITypePropertyPrivilege[] relationPropertyPrivileges = new ITypePropertyPrivilege[metaData
				.getRelationMembers().length];
		Arrays.fill(primitivePropertyPrivileges, defaultPropertyPrivilege);
		Arrays.fill(relationPropertyPrivileges, defaultPropertyPrivilege);
		for (int b = propertyPrivilegesOfService.length; b-- > 0;) {
			ITypePropertyPrivilegeOfService propertyPrivilegeOfService = propertyPrivilegesOfService[b];
			String propertyName = interningFeature.intern(propertyPrivilegeNames[b]);
			ITypePropertyPrivilege propertyPrivilege;
			if (propertyPrivilegeOfService != null) {
				propertyPrivilege = TypePropertyPrivilegeImpl.create(
						propertyPrivilegeOfService.isCreateAllowed(),
						propertyPrivilegeOfService.isReadAllowed(),
						propertyPrivilegeOfService.isUpdateAllowed(),
						propertyPrivilegeOfService.isDeleteAllowed());
			}
			else {
				propertyPrivilege = TypePropertyPrivilegeImpl.create(null, null, null, null);
			}
			if (metaData.isRelationMember(propertyName)) {
				relationPropertyPrivileges[metaData
						.getIndexByRelationName(propertyName)] = propertyPrivilege;
			}
			if (metaData.isPrimitiveMember(propertyName)) {
				primitivePropertyPrivileges[metaData
						.getIndexByPrimitiveName(propertyName)] = propertyPrivilege;
			}
		}
		return entityTypePrivilegeFactoryProvider
				.getEntityTypePrivilegeFactory(metaData.getEntityType(),
						privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(),
						privilegeOfService.isUpdateAllowed(), privilegeOfService.isDeleteAllowed(),
						privilegeOfService.isExecuteAllowed())
				.createPrivilege(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(),
						privilegeOfService.isUpdateAllowed(), privilegeOfService.isDeleteAllowed(),
						privilegeOfService.isExecuteAllowed(), primitivePropertyPrivileges,
						relationPropertyPrivileges);
	}

	@Override
	public ITypePrivilege getPrivilegeByType(Class<?> entityType) {
		return getPrivilegeByType(entityType, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public ITypePrivilege getPrivilegeByType(Class<?> entityType, ISecurityScope[] securityScopes) {
		ITypePrivilegeResult result = getPrivilegesByType(
				new ArrayList<Class<?>>(new Class<?>[] {entityType}), securityScopes);
		return result.getTypePrivileges()[0];
	}

	@Override
	public ITypePrivilegeResult getPrivilegesByType(List<Class<?>> entityTypes) {
		return getPrivilegesByType(entityTypes, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public ITypePrivilegeResult getPrivilegesByType(List<Class<?>> entityTypes,
			ISecurityScope[] securityScopes) {
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		if (securityScopes.length == 0) {
			throw new IllegalArgumentException(
					"No " + ISecurityScope.class.getSimpleName() + " provided to check privileges against");
		}
		ArrayList<Class<?>> missingEntityTypes = new ArrayList<>();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			ITypePrivilegeResult result = createResultByType(entityTypes, securityScopes,
					missingEntityTypes, authorization);
			if (missingEntityTypes.isEmpty()) {
				return result;
			}
		}
		finally {
			writeLock.unlock();
		}
		if (privilegeService == null) {
			throw new SecurityException("No bean of type " + IPrivilegeService.class.getName()
					+ " could be injected. Privilege functionality is deactivated. The current operation is not supported");
		}
		String userSID = authorization != null ? authorization.getSID() : null;
		List<ITypePrivilegeOfService> privilegeResults = privilegeService
				.getPrivilegesOfTypes(missingEntityTypes.toArray(Class.class), securityScopes);
		writeLock.lock();
		try {
			for (int a = 0, size = privilegeResults.size(); a < size; a++) {
				ITypePrivilegeOfService privilegeResult = privilegeResults.get(a);
				Class<?> entityType = privilegeResult.getEntityType();

				String securityScope = interningFeature
						.intern(privilegeResult.getSecurityScope().getName());

				ITypePrivilege pi = createTypePrivilegeFromServiceResult(entityType, privilegeResult);
				entityTypePrivilegeCache.put(entityType, securityScope, userSID, pi);
			}
			return createResultByType(entityTypes, securityScopes, null, authorization);
		}
		finally {
			writeLock.unlock();
		}
	}

	protected IPrivilegeResult createResult(List<? extends IObjRef> objRefs,
			ISecurityScope[] securityScopes, List<IObjRef> missingObjRefs, IAuthorization authorization,
			IMap<PrivilegeKey, IPrivilege> privilegeResultOfNewEntities) {
		PrivilegeKey privilegeKey = null;

		IPrivilege[] result = new IPrivilege[objRefs.size()];
		String userSID = authorization != null ? authorization.getSID() : null;

		for (int index = objRefs.size(); index-- > 0;) {
			IObjRef objRef = objRefs.get(index);
			if (objRef == null) {
				continue;
			}
			if (privilegeKey == null) {
				privilegeKey = new PrivilegeKey();
			}
			boolean useCache = true;
			privilegeKey.entityType = objRef.getRealType();
			privilegeKey.idIndex = objRef.getIdNameIndex();
			privilegeKey.id = objRef.getId();
			privilegeKey.userSID = userSID;
			if (privilegeKey.id == null) {
				useCache = false;
				// use the ObjRef instance as the id
				privilegeKey.id = objRef;
			}

			IPrivilege mergedPrivilegeItem = null;
			for (int a = securityScopes.length; a-- > 0;) {
				privilegeKey.securityScope = securityScopes[a].getName();

				IPrivilege existingPrivilegeItem = useCache ? privilegeCache.get(privilegeKey)
						: privilegeResultOfNewEntities != null ? privilegeResultOfNewEntities.get(privilegeKey)
								: null;
				if (existingPrivilegeItem == null) {
					mergedPrivilegeItem = null;
					break;
				}
				if (mergedPrivilegeItem == null) {
					// Take first existing privilege as a start
					mergedPrivilegeItem = existingPrivilegeItem;
				}
				else {
					// Merge all other existing privileges by boolean OR
					throw new UnsupportedOperationException("Not yet implemented");
				}
			}
			if (mergedPrivilegeItem == null) {
				if (missingObjRefs != null) {
					missingObjRefs.add(objRef);
					continue;
				}
				mergedPrivilegeItem = DenyAllPrivilege.INSTANCE;
			}
			result[index] = mergedPrivilegeItem;
		}
		return new PrivilegeResult(userSID, result);
	}

	protected ITypePrivilegeResult createResultByType(List<Class<?>> entityTypes,
			ISecurityScope[] securityScopes, List<Class<?>> missingEntityTypes,
			IAuthorization authorization) {
		ITypePrivilege[] result = new ITypePrivilege[entityTypes.size()];
		String userSID = authorization != null ? authorization.getSID() : null;

		for (int index = entityTypes.size(); index-- > 0;) {
			Class<?> entityType = entityTypes.get(index);
			if (entityType == null) {
				continue;
			}
			ITypePrivilege mergedTypePrivilege = null;
			for (int a = securityScopes.length; a-- > 0;) {
				ITypePrivilege existingTypePrivilege = entityTypePrivilegeCache.get(entityType,
						securityScopes[a].getName(), userSID);
				if (existingTypePrivilege == null) {
					mergedTypePrivilege = null;
					break;
				}
				if (mergedTypePrivilege == null) {
					// Take first existing privilege as a start
					mergedTypePrivilege = existingTypePrivilege;
				}
				else {
					// Merge all other existing privileges by boolean OR
					throw new UnsupportedOperationException("Not yet implemented");
				}
			}
			if (mergedTypePrivilege == null) {
				if (missingEntityTypes != null) {
					missingEntityTypes.add(entityType);
					continue;
				}
				mergedTypePrivilege = SkipAllTypePrivilege.INSTANCE;
			}
			result[index] = mergedTypePrivilege;
		}
		return new TypePrivilegeResult(userSID, result);
	}

	public void handleClearAllCaches(ClearAllCachesEvent evnt) {
		handleClearAllPrivileges(null);
	}

	public void handleClearAllPrivileges(ClearAllCachedPrivilegesEvent evnt) {
		writeLock.lock();
		try {
			privilegeCache.clear();
			entityTypePrivilegeCache.clear();
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId) {
		if (dataChange.isEmpty()) {
			return;
		}
		handleClearAllCaches(null);
	}
}
