package com.koch.ambeth.audit.server;

/*-
 * #%L
 * jambeth-audit-server
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import com.koch.ambeth.audit.model.AuditedEntityChangeType;
import com.koch.ambeth.audit.model.AuditedEntityPropertyItemChangeType;
import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRef;
import com.koch.ambeth.audit.model.IAuditedEntityRelationProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import com.koch.ambeth.audit.model.IAuditedService;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.audit.server.exceptions.AuditReasonMissingException;
import com.koch.ambeth.cache.IFirstLevelCacheManager;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ICUDResultApplier;
import com.koch.ambeth.merge.ICUDResultHelper;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.IMergeListener;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.MergeProcess;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.ICreateOrUpdateContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.RelationUpdateItemBuild;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.PrimitiveUpdateItem;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.api.database.ITransactionListener;
import com.koch.ambeth.security.IAuthenticatedUserHolder;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.ICurrentUserProvider;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IUserIdentifierProvider;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.stream.IInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.stream.chars.ICharacterInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputStream;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.format.XmlHint;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.NoOpStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;

public class AuditController implements IThreadLocalCleanupBean, IMethodCallLogger, IMergeListener,
		ITransactionListener, IAuditInfoController {
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditConfigurationProvider auditConfigurationProvider;

	@Autowired
	protected IAuthenticatedUserHolder authenticatedUserHolder;

	@Autowired(optional = true)
	protected IAuditEntryToSignature auditEntryToSignature;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected ICUDResultApplier cudResultApplier;

	@Autowired
	protected ICUDResultHelper cudResultHelper;

	@Autowired(optional = true)
	protected ICurrentUserProvider currentUserProvider;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IFirstLevelCacheManager firstLevelCacheManager;

	@Autowired
	protected IMergeService mergeService;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired(optional = true)
	protected IUserIdentifierProvider userIdentifierProvider;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ILightweightTransaction transaction;

	@Property(name = AuditConfigurationConstants.AuditedServiceDefaultModeActive, defaultValue = "true")
	protected boolean auditedServiceDefaultModeActive;

	@Property(name = SecurityServerConfigurationConstants.SignatureActive, defaultValue = "false")
	protected boolean signatureActive;

	@Forkable
	private final ThreadLocal<AdditionalAuditInfo> additionalAuditInfoTL = new ThreadLocal<>();

	@Forkable
	protected final ThreadLocal<AuditControllerState> auditEntryTL = new ThreadLocal<>();

	@Override
	public void cleanupThreadLocal() {
		if (auditEntryTL.get() != null || (additionalAuditInfoTL.get() != null
				&& additionalAuditInfoTL.get().clearTextPassword != null)) {
			throw new IllegalStateException("Should never contain a value at this point");
		}
		additionalAuditInfoTL.set(null);
	}

	protected AuditControllerState ensureAuditEntry() {
		AuditControllerState auditEntryState = auditEntryTL.get();
		if (auditEntryState != null) {
			return auditEntryState;
		}
		try {
			Long currentTime = database.getContextProvider().getCurrentTime();

			auditEntryState = new AuditControllerState(cudResultApplier.acquireNewState(null),
					entityMetaDataProvider);
			CreateOrUpdateContainerBuild auditEntry = auditEntryState.getAuditEntry();

			auditEntry.ensurePrimitive(IAuditEntry.Timestamp).setNewValue(currentTime);

			auditEntryTL.set(auditEntryState);
			return auditEntryState;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IMethodCallHandle logMethodCallStart(Method method, Object[] args) {
		AuditControllerState auditControllerState = ensureAuditEntry();

		CreateOrUpdateContainerBuild auditEntry = auditControllerState.auditedChanges.get(0);
		RelationUpdateItemBuild servicesRUI = auditEntry.ensureRelation(IAuditEntry.Services);

		CreateOrUpdateContainerBuild auditedService = auditControllerState
				.createEntity(IAuditedService.class);
		servicesRUI.addObjRef(auditedService.getReference());
		auditedService.ensureRelation(IAuditedService.Entry).addObjRef(auditEntry.getReference());

		auditedService.ensurePrimitive(IAuditedService.Order)
				.setNewValue(Integer.valueOf(servicesRUI.getAddedCount()));
		auditedService.ensurePrimitive(IAuditedService.ServiceType)
				.setNewValue(method.getDeclaringClass().getName());
		auditedService.ensurePrimitive(IAuditedService.MethodName).setNewValue(method.getName());
		auditedService.ensurePrimitive(IAuditedService.Arguments)
				.setNewValue(conversionHelper.convertValueToType(String[].class, args));

		return new MethodCallHandle(auditedService, System.currentTimeMillis());
	}

	@Override
	public void logMethodCallFinish(IMethodCallHandle methodCallHandle) {
		if (methodCallHandle == null) {
			return;
		}
		MethodCallHandle handle = (MethodCallHandle) methodCallHandle;
		handle.auditedService.ensurePrimitive(IAuditedService.SpentTime)
				.setNewValue(Long.valueOf(System.currentTimeMillis() - handle.start));
	}

	@Override
	public ICUDResult preMerge(ICUDResult cudResult, ICache cache) {
		// intended blank
		return cudResult;
	}

	@Override
	public void postMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs) {
		if (Boolean.TRUE.equals(getAdditionalAuditInfo().ownAuditMergeActive)) {
			// ignore this dataChange because it is our own Audit merge
			return;
		}
		AuditControllerState auditControllerState = ensureAuditEntry();

		List<Object> originalRefs = cudResult.getOriginalRefs();
		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		HashMap<IObjRef, IDirectObjRef> objRefToRefMap = new HashMap<>();

		for (int index = allChanges.size(); index-- > 0;) {
			IChangeContainer changeContainer = allChanges.get(index);
			IObjRef updatedObjRef = updatedObjRefs[index];
			Object originalRef = originalRefs.get(index);
			auditChangeContainer(originalRef, updatedObjRef, changeContainer, auditControllerState,
					objRefToRefMap);
		}
	}

	protected void auditChangeContainer(Object originalRef, IObjRef updatedObjRef,
			IChangeContainer changeContainer, AuditControllerState auditControllerState,
			IMap<IObjRef, IDirectObjRef> objRefToRefMap) {
		IObjRef objRef = changeContainer.getReference();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
		IAuditConfiguration auditConfiguration = auditConfigurationProvider
				.getAuditConfiguration(metaData.getEntityType());
		if (auditConfiguration == null || !auditConfiguration.isAuditActive()) {
			return;
		}

		// test if audit reason is required and throw exception if its not set
		if (auditConfiguration.isReasonRequired() && peekAuditReason() == null) {
			throw new AuditReasonMissingException(
					"Audit reason is missing for " + originalRef.getClass() + "!");
		}
		CreateOrUpdateContainerBuild auditedEntity = auditControllerState
				.createEntity(IAuditedEntity.class);
		CreateOrUpdateContainerBuild auditEntry = auditControllerState.auditedChanges.get(0);
		RelationUpdateItemBuild entities = auditEntry.ensureRelation(IAuditEntry.Entities);
		entities.addObjRef(auditedEntity.getReference());

		auditedEntity.ensureRelation(IAuditedEntity.Entry).addObjRef(auditEntry.getReference());
		auditedEntity.ensurePrimitive(IAuditedEntity.Order).setNewValue(entities.getAddedCount());

		if (changeContainer instanceof CreateContainer) {
			auditedEntity.ensurePrimitive(IAuditedEntity.ChangeType)
					.setNewValue(AuditedEntityChangeType.INSERT);
			auditedEntity.ensureRelation(IAuditedEntity.Ref)
					.addObjRef(getOrCreateRef(updatedObjRef, auditControllerState, objRefToRefMap));
			auditPUIs(updatedObjRef.getRealType(), ((CreateContainer) changeContainer).getPrimitives(),
					auditedEntity, auditConfiguration, auditControllerState);
			auditRUIs(((CreateContainer) changeContainer).getRelations(), auditedEntity,
					auditConfiguration, auditControllerState, objRefToRefMap);
		}
		else if (changeContainer instanceof UpdateContainer) {
			auditedEntity.ensurePrimitive(IAuditedEntity.ChangeType)
					.setNewValue(AuditedEntityChangeType.UPDATE);
			auditedEntity.ensureRelation(IAuditedEntity.Ref)
					.addObjRef(getOrCreateRef(updatedObjRef, auditControllerState, objRefToRefMap));
			auditPUIs(updatedObjRef.getRealType(), ((UpdateContainer) changeContainer).getPrimitives(),
					auditedEntity, auditConfiguration, auditControllerState);
			auditRUIs(((UpdateContainer) changeContainer).getRelations(), auditedEntity,
					auditConfiguration, auditControllerState, objRefToRefMap);
		}
		else if (changeContainer instanceof DeleteContainer) {
			auditedEntity.ensurePrimitive(IAuditedEntity.ChangeType)
					.setNewValue(AuditedEntityChangeType.DELETE);
			auditedEntity.ensureRelation(IAuditedEntity.Ref)
					.addObjRef(getOrCreateRef(objRef, auditControllerState, objRefToRefMap));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected IDirectObjRef getOrCreateRef(IObjRef objRef, AuditControllerState auditControllerState,
			IMap<IObjRef, IDirectObjRef> objRefToRefMap) {
		IDirectObjRef ref = objRefToRefMap.get(objRef);
		if (ref != null) {
			IObjRef existingObjRef = objRefToRefMap.getKey(objRef);
			if (((Comparable) existingObjRef.getVersion()).compareTo(objRef.getVersion()) >= 0) {
				return ref;
			}
			objRefToRefMap.put(objRef, ref);
			PrimitiveUpdateItem entityVersion = ((CreateOrUpdateContainerBuild) ref.getDirect())
					.findPrimitive(IAuditedEntityRef.EntityVersion);
			entityVersion
					.setNewValue(conversionHelper.convertValueToType(String.class, objRef.getVersion()));
			return ref;
		}
		CreateOrUpdateContainerBuild auditedEntityRef = auditControllerState
				.createEntity(IAuditedEntityRef.class);
		ref = (IDirectObjRef) auditedEntityRef.getReference();

		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityId)
				.setNewValue(conversionHelper.convertValueToType(String.class, objRef.getId()));
		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityType)
				.setNewValue(objRef.getRealType());
		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityVersion)
				.setNewValue(conversionHelper.convertValueToType(String.class, objRef.getVersion()));

		objRefToRefMap.put(objRef, ref);
		return ref;
	}

	protected void auditPUIs(Class<?> entityType, IPrimitiveUpdateItem[] puis,
			CreateOrUpdateContainerBuild auditedEntity, IAuditConfiguration auditConfiguration,
			AuditControllerState auditControllerState) {
		if (puis == null) {
			return;
		}
		ITableMetaData table = null;
		for (IPrimitiveUpdateItem pui : puis) {
			if (!auditConfiguration.getMemberConfiguration(pui.getMemberName()).isAuditActive()) {
				continue;
			}
			CreateOrUpdateContainerBuild primitiveProperty = auditControllerState
					.createEntity(IAuditedEntityPrimitiveProperty.class);
			RelationUpdateItemBuild primitives = auditedEntity.ensureRelation(IAuditedEntity.Primitives);
			primitives.addObjRef(primitiveProperty.getReference());
			primitiveProperty.ensureRelation(IAuditedEntityPrimitiveProperty.Entity)
					.addObjRef(auditedEntity.getReference());

			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.Name)
					.setNewValue(pui.getMemberName());

			String auditedValue = createAuditedValueOfEntityPrimitive(pui.getNewValue());
			if (auditedValue != null && auditedValue.length() == 0) {
				if (table == null) {
					table = databaseMetaData.getTableByType(entityType);
				}
				IFieldMetaData field = table.getFieldByPropertyName(pui.getMemberName());
				if (connectionDialect.isEmptyStringAsNullStored(field)) {
					auditedValue = null;
				}
			}
			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.NewValue)
					.setNewValue(auditedValue);
			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.Order)
					.setNewValue(Integer.valueOf(primitives.getAddedCount()));
		}
	}

	@Override
	public String createAuditedValueOfEntityPrimitive(Object primitiveValueOfEntity) {
		if (!(primitiveValueOfEntity instanceof IInputSource)) {
			return conversionHelper.convertValueToType(String.class, primitiveValueOfEntity,
					XmlHint.WRITE_ATTRIBUTE);
		}
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try {
			if (primitiveValueOfEntity instanceof IBinaryInputSource) {
				IBinaryInputStream is = ((IBinaryInputSource) primitiveValueOfEntity)
						.deriveBinaryInputStream();
				try {
					int oneByte;
					while ((oneByte = is.readByte()) != -1) {
						sb.append((char) oneByte);
					}
					return sb.toString();
				}
				finally {
					try {
						is.close();
					}
					catch (IOException e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			}
			else if (primitiveValueOfEntity instanceof ICharacterInputSource) {
				ICharacterInputStream is = ((ICharacterInputSource) primitiveValueOfEntity)
						.deriveCharacterInputStream();
				try {
					int oneByte;
					while ((oneByte = is.readChar()) != -1) {
						sb.append((char) oneByte);
					}
					return sb.toString();
				}
				finally {
					try {
						is.close();
					}
					catch (IOException e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			}
			throw new IllegalArgumentException("Can not audit value '" + primitiveValueOfEntity + "'");
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	protected void auditRUIs(IRelationUpdateItem[] ruis, CreateOrUpdateContainerBuild auditedEntity,
			IAuditConfiguration auditConfiguration, AuditControllerState auditControllerState,
			IMap<IObjRef, IDirectObjRef> objRefToRefMap) {
		if (ruis == null) {
			return;
		}
		for (IRelationUpdateItem rui : ruis) {
			if (!auditConfiguration.getMemberConfiguration(rui.getMemberName()).isAuditActive()) {
				continue;
			}
			CreateOrUpdateContainerBuild relationProperty = auditControllerState
					.createEntity(IAuditedEntityRelationProperty.class);
			RelationUpdateItemBuild relations = auditedEntity.ensureRelation(IAuditedEntity.Relations);
			relations.addObjRef(relationProperty.getReference());
			relationProperty.ensureRelation(IAuditedEntityRelationProperty.Entity)
					.addObjRef(auditedEntity.getReference());

			relationProperty.ensurePrimitive(IAuditedEntityRelationProperty.Name)
					.setNewValue(rui.getMemberName());
			relationProperty.ensurePrimitive(IAuditedEntityRelationProperty.Order)
					.setNewValue(Integer.valueOf(relations.getAddedCount()));

			IObjRef[] addedORIs = rui.getAddedORIs();
			if (addedORIs != null) {
				for (IObjRef addedORI : addedORIs) {
					auditPropertyItem(addedORI, relationProperty, AuditedEntityPropertyItemChangeType.ADD,
							auditControllerState, objRefToRefMap);
				}
			}
			IObjRef[] removedORIs = rui.getRemovedORIs();
			if (removedORIs != null) {
				for (IObjRef removedORI : removedORIs) {
					auditPropertyItem(removedORI, relationProperty,
							AuditedEntityPropertyItemChangeType.REMOVE, auditControllerState, objRefToRefMap);
				}
			}
		}
	}

	protected void auditPropertyItem(IObjRef objRef, CreateOrUpdateContainerBuild relationProperty,
			AuditedEntityPropertyItemChangeType changeType, AuditControllerState auditControllerState,
			IMap<IObjRef, IDirectObjRef> objRefToRefMap) {
		CreateOrUpdateContainerBuild propertyItem = auditControllerState
				.createEntity(IAuditedEntityRelationPropertyItem.class);
		RelationUpdateItemBuild items = relationProperty
				.ensureRelation(IAuditedEntityRelationProperty.Items);
		items.addObjRef(propertyItem.getReference());

		propertyItem.ensureRelation(IAuditedEntityRelationPropertyItem.Ref)
				.addObjRef(getOrCreateRef(objRef, auditControllerState, objRefToRefMap));
		propertyItem.ensurePrimitive(IAuditedEntityRelationPropertyItem.ChangeType)
				.setNewValue(changeType);
		propertyItem.ensurePrimitive(IAuditedEntityRelationPropertyItem.Order)
				.setNewValue(Integer.valueOf(items.getAddedCount()));
	}

	@Override
	public void handlePostBegin(long sessionId) throws Exception {
		// intended blank
	}

	@Override
	public void handlePostRollback(long sessionId) throws Exception {
		auditEntryTL.set(null);
		AdditionalAuditInfo additionalAuditInfo = additionalAuditInfoTL.get();
		if (additionalAuditInfo != null && additionalAuditInfo.doClearPassword) {
			additionalAuditInfo.clearTextPassword = null;
		}
	}

	@Override
	public void handlePreCommit(long sessionId) throws Exception {
		final AuditControllerState auditEntryState = auditEntryTL.get();
		if (auditEntryState == null) {
			handlePostRollback(sessionId);
			return;
		}
		auditEntryTL.set(null);

		AdditionalAuditInfo additionalAuditInfo = getAdditionalAuditInfo();
		additionalAuditInfo.ownAuditMergeActive = Boolean.TRUE;
		try {
			securityActivation.executeWithoutSecurity(new IBackgroundWorkerDelegate() {
				@Override
				public void invoke() throws Exception {
					ArrayList<CreateOrUpdateContainerBuild> auditedChanges = auditEntryState.auditedChanges;
					CreateOrUpdateContainerBuild auditEntry = auditedChanges.get(0);

					RelationUpdateItemBuild entities = auditEntry.findRelation(IAuditEntry.Entities);
					RelationUpdateItemBuild services = auditEntry.findRelation(IAuditEntry.Services);
					if ((entities == null || entities.getAddedCount() == 0)
							&& (services == null || services.getAddedCount() == 0)) {
						// No entity changed, no service called
						return;
					}

					auditEntry.ensurePrimitive(IAuditEntry.Context).setNewValue(peekAuditContext());
					auditEntry.ensurePrimitive(IAuditEntry.Reason).setNewValue(peekAuditReason());
					AdditionalAuditInfo additionalAuditInfo = additionalAuditInfoTL.get();
					IUser authorizedUser = getAuthorizedUser(additionalAuditInfo);
					ISignature signatureOfUser = null;
					if (authorizedUser != null) {
						auditEntry.ensureRelation(IAuditEntry.User)
								.addObjRef(objRefHelper.entityToObjRef(authorizedUser));
						auditEntry.ensurePrimitive(IAuditEntry.UserIdentifier)
								.setNewValue(userIdentifierProvider.getSID(authorizedUser));
						signatureOfUser = authorizedUser.getSignature();
					}
					signAuditEntry(auditEntry, additionalAuditInfo, signatureOfUser);
					ICUDResult auditMerge = buildAuditCUDResult(auditedChanges);

					Boolean oldAddNewlyPersistedEntities = MergeProcess.getAddNewlyPersistedEntities();
					MergeProcess.setAddNewlyPersistedEntities(Boolean.FALSE);
					try {
						mergeService.merge(auditMerge, null);
					}
					finally {
						MergeProcess.setAddNewlyPersistedEntities(oldAddNewlyPersistedEntities);
					}

					return;
				}
			});
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			additionalAuditInfo.ownAuditMergeActive = null;
			if (additionalAuditInfo.doClearPassword) {
				additionalAuditInfo.clearTextPassword = null;
			}
		}
	}

	public AdditionalAuditInfo getAdditionalAuditInfo() {
		AdditionalAuditInfo additionalAuditInfo = additionalAuditInfoTL.get();
		if (additionalAuditInfo == null) {
			additionalAuditInfo = new AdditionalAuditInfo();
			additionalAuditInfoTL.set(additionalAuditInfo);
		}
		return additionalAuditInfo;
	}

	@Override
	public void removeAuditInfo() {
		additionalAuditInfoTL.set(null);
		authenticatedUserHolder.setAuthenticatedSID(null);
	}

	@Override
	public void pushAuditReason(String auditReason) {
		getAdditionalAuditInfo().auditReasonContainer.add(auditReason);
	}

	@Override
	public String popAuditReason() {
		return getAdditionalAuditInfo().auditReasonContainer.popLastElement();
	}

	@Override
	public String peekAuditReason() {
		return getAdditionalAuditInfo().auditReasonContainer.peek();
	}

	@Override
	public void pushAuditContext(String auditContext) {
		getAdditionalAuditInfo().auditContextContainer.add(auditContext);
	}

	@Override
	public String popAuditContext() {
		return getAdditionalAuditInfo().auditContextContainer.popLastElement();
	}

	@Override
	public String peekAuditContext() {
		return getAdditionalAuditInfo().auditContextContainer.peek();
	}

	@Override
	public IStateRollback pushClearTextPassword(final char[] clearTextPassword,
			IStateRollback... rollbacks) {
		final AdditionalAuditInfo additionalAuditInfo = getAdditionalAuditInfo();
		final char[] oldClearTextPassword = additionalAuditInfo.clearTextPassword;
		final boolean oldDoClearPassword = additionalAuditInfo.doClearPassword;
		additionalAuditInfo.clearTextPassword = clearTextPassword;
		additionalAuditInfo.doClearPassword = false;
		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Exception {
				if (additionalAuditInfo.clearTextPassword != clearTextPassword) {
					throw new IllegalStateException("Illegal state: clearTextPassword does not match");
				}
				additionalAuditInfo.clearTextPassword = oldClearTextPassword;
				additionalAuditInfo.doClearPassword = oldDoClearPassword;
			}
		};
	}

	@Override
	public IStateRollback pushAuthorizedUser(final IUser user, final char[] clearTextPassword,
			boolean forceGivenAuthorization, IStateRollback... rollbacks) {
		final AdditionalAuditInfo additionalAuditInfo = getAdditionalAuditInfo();
		final IUser oldAuthorizedUser = additionalAuditInfo.authorizedUser;
		if (!forceGivenAuthorization && (oldAuthorizedUser != null
				|| (currentUserProvider != null && currentUserProvider.getCurrentUser() != null))) {
			// do nothing
			return NoOpStateRollback.createNoOpRollback(rollbacks);
		}
		final String oldAuthorizedUserSID = authenticatedUserHolder.getAuthenticatedSID();
		final char[] oldClearTextPassword = additionalAuditInfo.clearTextPassword;
		final String sid = userIdentifierProvider.getSID(user);

		additionalAuditInfo.authorizedUser = user;
		additionalAuditInfo.clearTextPassword = clearTextPassword;
		additionalAuditInfo.doClearPassword = false;
		authenticatedUserHolder.setAuthenticatedSID(sid);
		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Exception {
				String authenticatedSID = authenticatedUserHolder.getAuthenticatedSID();
				if (authenticatedSID != sid && authenticatedSID != null) {
					throw new IllegalStateException("Illegal state: authorizedUserSID does not match");
				}
				authenticatedUserHolder.setAuthenticatedSID(oldAuthorizedUserSID);

				if (additionalAuditInfo.authorizedUser != user) {
					throw new IllegalStateException("Illegal state: user does not match");
				}
				if (additionalAuditInfo.clearTextPassword != clearTextPassword) {
					throw new IllegalStateException("Illegal state: clearTextPassword does not match");
				}
				additionalAuditInfo.clearTextPassword = oldClearTextPassword;
				additionalAuditInfo.authorizedUser = oldAuthorizedUser;
			}
		};
	}

	protected IUser getAuthorizedUser(AdditionalAuditInfo additionalAuditInfo) {
		IUser authorizedUser = null;
		if (additionalAuditInfo != null) {
			authorizedUser = additionalAuditInfo.authorizedUser;
		}
		if (authorizedUser == null && currentUserProvider != null) {
			authorizedUser = currentUserProvider.getCurrentUser();
		}
		return authorizedUser;
	}

	protected void signAuditEntry(CreateOrUpdateContainerBuild auditEntry,
			AdditionalAuditInfo additionalAuditInfo, ISignature signatureOfUser) {
		if (signatureOfUser == null && signatureActive) {
			throw new IllegalStateException(
					"Failed to create an Audit Entry without a signature - which is mandatory because of '"
							+ SecurityServerConfigurationConstants.SignatureActive + "'=true");
		}
		char[] clearTextPassword = additionalAuditInfo.clearTextPassword;
		if (clearTextPassword == null && signatureActive) {
			ISecurityContext securityContext = securityContextHolder.getContext();
			IAuthentication authentication = securityContext != null ? securityContext.getAuthentication()
					: null;
			if (authentication != null) {
				clearTextPassword = authentication.getPassword();
			}
		}
		if (clearTextPassword == null && signatureActive) {
			throw new IllegalStateException(
					"Failed to create an Audit Entry for a signature without a given cleartext password to decrypt the private key");
		}
		auditEntryToSignature.signAuditEntry(auditEntry, clearTextPassword, signatureOfUser);
	}

	protected ICUDResult buildAuditCUDResult(IList<CreateOrUpdateContainerBuild> auditedChanges) {
		ArrayList<IChangeContainer> finalizedAuditChanges = new ArrayList<>(auditedChanges.size());
		for (int a = 0, size = auditedChanges.size(); a < size; a++) {
			CreateOrUpdateContainerBuild createOrUpdate = auditedChanges.get(a);
			if (!createOrUpdate.isCreate()) {
				throw new IllegalStateException();
			}
			ICreateOrUpdateContainer cc = createOrUpdate.build();
			((IDirectObjRef) cc.getReference()).setDirect(cc);
			IRelationUpdateItem[] ruis = cc.getFullRUIs();
			if (ruis != null) {
				for (int b = ruis.length; b-- > 0;) {
					IRelationUpdateItem rui = ruis[b];
					if (rui instanceof RelationUpdateItemBuild) {
						ruis[b] = ((RelationUpdateItemBuild) rui).buildRUI();
					}
				}
			}
			finalizedAuditChanges.add(cc);
		}
		return new CUDResult(finalizedAuditChanges, new ArrayList<>(new Object[auditedChanges.size()]));
	}
}
