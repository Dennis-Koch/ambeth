package de.osthus.ambeth.audit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import de.osthus.ambeth.audit.model.AuditedEntityChangeType;
import de.osthus.ambeth.audit.model.AuditedEntityPropertyItemChangeType;
import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRef;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import de.osthus.ambeth.audit.model.IAuditedService;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.IFirstLevelCacheManager;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransactionListener;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.exceptions.AuditReasonMissingException;
import de.osthus.ambeth.format.XmlHint;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ICUDResultApplier;
import de.osthus.ambeth.merge.ICUDResultHelper;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeListener;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.MergeProcess;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.ICreateOrUpdateContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.model.RelationUpdateItemBuild;
import de.osthus.ambeth.merge.transfer.CUDResult;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.PrimitiveUpdateItem;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.IAuthorizedUserHolder;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.IUserIdentifierProvider;
import de.osthus.ambeth.security.IUserResolver;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.stream.IInputSource;
import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.stream.chars.ICharacterInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IConversionHelper;

public class AuditController implements IThreadLocalCleanupBean, IMethodCallLogger, IMergeListener, ITransactionListener, IAuditInfoController
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditConfigurationProvider auditConfigurationProvider;

	@Autowired
	protected IAuthorizedUserHolder authorizedUserHolder;

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

	@Autowired(optional = true)
	protected IUserResolver userResolver;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Property(name = AuditConfigurationConstants.AuditedServiceDefaultModeActive, defaultValue = "true")
	protected boolean auditedServiceDefaultModeActive;

	@Forkable
	private final ThreadLocal<AdditionalAuditInfo> additionalAuditInfoTL = new ThreadLocal<AdditionalAuditInfo>();

	@Forkable
	protected final ThreadLocal<AuditControllerState> auditEntryTL = new ThreadLocal<AuditControllerState>();

	@Override
	public void cleanupThreadLocal()
	{
		if (auditEntryTL.get() != null || (additionalAuditInfoTL.get() != null && additionalAuditInfoTL.get().clearTextPassword != null))
		{
			throw new IllegalStateException("Should never contain a value at this point");
		}
		additionalAuditInfoTL.set(null);
	}

	protected AuditControllerState ensureAuditEntry()
	{
		AuditControllerState auditEntryState = auditEntryTL.get();
		if (auditEntryState != null)
		{
			return auditEntryState;
		}
		try
		{
			Long currentTime = database.getContextProvider().getCurrentTime();

			auditEntryState = new AuditControllerState(cudResultApplier.acquireNewState(null), entityMetaDataProvider);
			CreateOrUpdateContainerBuild auditEntry = auditEntryState.getAuditEntry();

			auditEntry.ensurePrimitive(IAuditEntry.Timestamp).setNewValue(currentTime.longValue());
			auditEntry.ensurePrimitive(IAuditEntry.Context).setNewValue(peekAuditContext());
			auditEntry.ensurePrimitive(IAuditEntry.Reason).setNewValue(peekAuditReason());

			AdditionalAuditInfo additionalAuditInfo = additionalAuditInfoTL.get();
			IUser authorizedUser = null;
			if (additionalAuditInfo != null)
			{
				authorizedUser = additionalAuditInfo.authorizedUser;
			}
			if (authorizedUser == null)
			{
				ISecurityContext context = securityContextHolder.getContext();
				IAuthorization authorization = context != null ? context.getAuthorization() : null;
				if (authorization != null)
				{
					final String currentSID = authorization.getSID();
					authorizedUser = securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<IUser>()
					{
						@Override
						public IUser invoke() throws Throwable
						{
							return userResolver.resolveUserBySID(currentSID);
						}
					});
				}
			}
			if (authorizedUser != null)
			{
				auditEntry.ensureRelation(IAuditEntry.User).addObjRef(objRefHelper.entityToObjRef(authorizedUser));
				auditEntry.ensurePrimitive(IAuditEntry.UserIdentifier).setNewValue(userIdentifierProvider.getSID(authorizedUser));

				ISignature signatureOfUser = authorizedUser.getSignature();
				if (signatureOfUser != null)
				{
					auditEntryState.setSignatureOfUser(signatureOfUser);
				}
			}
			auditEntryTL.set(auditEntryState);
			return auditEntryState;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IMethodCallHandle logMethodCallStart(Method method, Object[] args)
	{
		AuditControllerState auditControllerState = ensureAuditEntry();

		CreateOrUpdateContainerBuild auditEntry = auditControllerState.auditedChanges.get(0);
		RelationUpdateItemBuild servicesRUI = auditEntry.ensureRelation(IAuditEntry.Services);

		CreateOrUpdateContainerBuild auditedService = auditControllerState.createEntity(IAuditedService.class);
		servicesRUI.addObjRef(auditedService.getReference());
		auditedService.ensureRelation(IAuditedService.Entry).addObjRef(auditEntry.getReference());

		auditedService.ensurePrimitive(IAuditedService.Order).setNewValue(Integer.valueOf(servicesRUI.getAddedCount()));
		auditedService.ensurePrimitive(IAuditedService.ServiceType).setNewValue(method.getDeclaringClass().getName());
		auditedService.ensurePrimitive(IAuditedService.MethodName).setNewValue(method.getName());
		auditedService.ensurePrimitive(IAuditedService.Arguments).setNewValue(conversionHelper.convertValueToType(String[].class, args));

		return new MethodCallHandle(auditedService, System.currentTimeMillis());
	}

	@Override
	public void logMethodCallFinish(IMethodCallHandle methodCallHandle)
	{
		if (methodCallHandle == null)
		{
			return;
		}
		MethodCallHandle handle = (MethodCallHandle) methodCallHandle;
		handle.auditedService.ensurePrimitive(IAuditedService.SpentTime).setNewValue(Long.valueOf(System.currentTimeMillis() - handle.start));
	}

	@Override
	public ICUDResult preMerge(ICUDResult cudResult, ICache cache)
	{
		// intended blank
		return cudResult;
	}

	@Override
	public void postMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs)
	{
		if (Boolean.TRUE.equals(getAdditionalAuditInfo().ownAuditMergeActive))
		{
			// ignore this dataChange because it is our own Audit merge
			return;
		}
		AuditControllerState auditControllerState = ensureAuditEntry();

		List<Object> originalRefs = cudResult.getOriginalRefs();
		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		HashMap<IObjRef, IDirectObjRef> objRefToRefMap = new HashMap<IObjRef, IDirectObjRef>();

		for (int index = allChanges.size(); index-- > 0;)
		{
			IChangeContainer changeContainer = allChanges.get(index);
			IObjRef updatedObjRef = updatedObjRefs[index];
			Object originalRef = originalRefs.get(index);
			auditChangeContainer(originalRef, updatedObjRef, changeContainer, auditControllerState, objRefToRefMap);
		}
	}

	protected void auditChangeContainer(Object originalRef, IObjRef updatedObjRef, IChangeContainer changeContainer, AuditControllerState auditControllerState,
			IMap<IObjRef, IDirectObjRef> objRefToRefMap)
	{
		IObjRef objRef = changeContainer.getReference();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
		IAuditConfiguration auditConfiguration = auditConfigurationProvider.getAuditConfiguration(metaData.getEntityType());
		if (auditConfiguration == null)
		{
			return;
		}

		// test if audit reason is required and throw exception if its not set
		if (auditConfiguration.isReasonRequired() && peekAuditReason() == null)
		{
			throw new AuditReasonMissingException("Audit reason is missing for " + originalRef.getClass() + "!");
		}
		CreateOrUpdateContainerBuild auditedEntity = auditControllerState.createEntity(IAuditedEntity.class);
		CreateOrUpdateContainerBuild auditEntry = auditControllerState.auditedChanges.get(0);
		RelationUpdateItemBuild entities = auditEntry.ensureRelation(IAuditEntry.Entities);
		entities.addObjRef(auditedEntity.getReference());

		auditedEntity.ensureRelation(IAuditedEntity.Entry).addObjRef(auditEntry.getReference());
		auditedEntity.ensurePrimitive(IAuditedEntity.Order).setNewValue(entities.getAddedCount());

		if (changeContainer instanceof CreateContainer)
		{
			auditedEntity.ensurePrimitive(IAuditedEntity.ChangeType).setNewValue(AuditedEntityChangeType.INSERT);
			auditedEntity.ensureRelation(IAuditedEntity.Ref).addObjRef(getOrCreateRef(updatedObjRef, auditControllerState, objRefToRefMap));
			auditPUIs(updatedObjRef.getRealType(), ((CreateContainer) changeContainer).getPrimitives(), auditedEntity, auditConfiguration, auditControllerState);
			auditRUIs(((CreateContainer) changeContainer).getRelations(), auditedEntity, auditConfiguration, auditControllerState, objRefToRefMap);
		}
		else if (changeContainer instanceof UpdateContainer)
		{
			auditedEntity.ensurePrimitive(IAuditedEntity.ChangeType).setNewValue(AuditedEntityChangeType.UPDATE);
			auditedEntity.ensureRelation(IAuditedEntity.Ref).addObjRef(getOrCreateRef(updatedObjRef, auditControllerState, objRefToRefMap));
			auditPUIs(updatedObjRef.getRealType(), ((UpdateContainer) changeContainer).getPrimitives(), auditedEntity, auditConfiguration, auditControllerState);
			auditRUIs(((UpdateContainer) changeContainer).getRelations(), auditedEntity, auditConfiguration, auditControllerState, objRefToRefMap);
		}
		else if (changeContainer instanceof DeleteContainer)
		{
			auditedEntity.ensurePrimitive(IAuditedEntity.ChangeType).setNewValue(AuditedEntityChangeType.DELETE);
			auditedEntity.ensureRelation(IAuditedEntity.Ref).addObjRef(getOrCreateRef(objRef, auditControllerState, objRefToRefMap));
		}
	}

	protected IDirectObjRef getOrCreateRef(IObjRef objRef, AuditControllerState auditControllerState, IMap<IObjRef, IDirectObjRef> objRefToRefMap)
	{
		IDirectObjRef ref = objRefToRefMap.get(objRef);
		if (ref != null)
		{
			IObjRef existingObjRef = objRefToRefMap.getKey(objRef);
			if (((Comparable) existingObjRef.getVersion()).compareTo(objRef.getVersion()) >= 0)
			{
				return ref;
			}
			objRefToRefMap.put(objRef, ref);
			PrimitiveUpdateItem entityVersion = ((CreateOrUpdateContainerBuild) ref.getDirect()).findPrimitive(IAuditedEntityRef.EntityVersion);
			entityVersion.setNewValue(conversionHelper.convertValueToType(String.class, objRef.getVersion()));
			return ref;
		}
		CreateOrUpdateContainerBuild auditedEntityRef = auditControllerState.createEntity(IAuditedEntityRef.class);
		ref = (IDirectObjRef) auditedEntityRef.getReference();

		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityId).setNewValue(conversionHelper.convertValueToType(String.class, objRef.getId()));
		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityType).setNewValue(objRef.getRealType());
		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityVersion).setNewValue(conversionHelper.convertValueToType(String.class, objRef.getVersion()));

		objRefToRefMap.put(objRef, ref);
		return ref;
	}

	protected void auditPUIs(Class<?> entityType, IPrimitiveUpdateItem[] puis, CreateOrUpdateContainerBuild auditedEntity,
			IAuditConfiguration auditConfiguration, AuditControllerState auditControllerState)
	{
		if (puis == null)
		{
			return;
		}
		ITableMetaData table = null;
		for (IPrimitiveUpdateItem pui : puis)
		{
			if (!auditConfiguration.getMemberConfiguration(pui.getMemberName()).isAuditActive())
			{
				continue;
			}
			CreateOrUpdateContainerBuild primitiveProperty = auditControllerState.createEntity(IAuditedEntityPrimitiveProperty.class);
			RelationUpdateItemBuild primitives = auditedEntity.ensureRelation(IAuditedEntity.Primitives);
			primitives.addObjRef(primitiveProperty.getReference());
			primitiveProperty.ensureRelation(IAuditedEntityPrimitiveProperty.Entity).addObjRef(auditedEntity.getReference());

			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.Name).setNewValue(pui.getMemberName());

			String auditedValue = createAuditedValueOfEntityPrimitive(pui.getNewValue());
			if (auditedValue.length() == 0)
			{
				if (table == null)
				{
					table = databaseMetaData.getTableByType(entityType);
				}
				IFieldMetaData field = table.getFieldByPropertyName(pui.getMemberName());
				if (connectionDialect.isEmptyStringAsNullStored(field))
				{
					auditedValue = null;
				}
			}
			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.NewValue).setNewValue(auditedValue);
			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.Order).setNewValue(Integer.valueOf(primitives.getAddedCount()));
		}
	}

	@Override
	public String createAuditedValueOfEntityPrimitive(Object primitiveValueOfEntity)
	{
		if (!(primitiveValueOfEntity instanceof IInputSource))
		{
			return conversionHelper.convertValueToType(String.class, primitiveValueOfEntity, XmlHint.WRITE_ATTRIBUTE);
		}
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			if (primitiveValueOfEntity instanceof IBinaryInputSource)
			{
				IBinaryInputStream is = ((IBinaryInputSource) primitiveValueOfEntity).deriveBinaryInputStream();
				try
				{
					int oneByte;
					while ((oneByte = is.readByte()) != -1)
					{
						sb.append((char) oneByte);
					}
					return sb.toString();
				}
				finally
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			}
			else if (primitiveValueOfEntity instanceof ICharacterInputSource)
			{
				ICharacterInputStream is = ((ICharacterInputSource) primitiveValueOfEntity).deriveCharacterInputStream();
				try
				{
					int oneByte;
					while ((oneByte = is.readChar()) != -1)
					{
						sb.append((char) oneByte);
					}
					return sb.toString();
				}
				finally
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			}
			throw new IllegalArgumentException("Can not audit value '" + primitiveValueOfEntity + "'");
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	protected void auditRUIs(IRelationUpdateItem[] ruis, CreateOrUpdateContainerBuild auditedEntity, IAuditConfiguration auditConfiguration,
			AuditControllerState auditControllerState, IMap<IObjRef, IDirectObjRef> objRefToRefMap)
	{
		if (ruis == null)
		{
			return;
		}
		for (IRelationUpdateItem rui : ruis)
		{
			if (!auditConfiguration.getMemberConfiguration(rui.getMemberName()).isAuditActive())
			{
				continue;
			}
			CreateOrUpdateContainerBuild relationProperty = auditControllerState.createEntity(IAuditedEntityRelationProperty.class);
			RelationUpdateItemBuild relations = auditedEntity.ensureRelation(IAuditedEntity.Relations);
			relations.addObjRef(relationProperty.getReference());
			relationProperty.ensureRelation(IAuditedEntityRelationProperty.Entity).addObjRef(auditedEntity.getReference());

			relationProperty.ensurePrimitive(IAuditedEntityRelationProperty.Name).setNewValue(rui.getMemberName());
			relationProperty.ensurePrimitive(IAuditedEntityRelationProperty.Order).setNewValue(Integer.valueOf(relations.getAddedCount()));

			IObjRef[] addedORIs = rui.getAddedORIs();
			if (addedORIs != null)
			{
				for (IObjRef addedORI : addedORIs)
				{
					auditPropertyItem(addedORI, relationProperty, AuditedEntityPropertyItemChangeType.ADD, auditControllerState, objRefToRefMap);
				}
			}
			IObjRef[] removedORIs = rui.getRemovedORIs();
			if (removedORIs != null)
			{
				for (IObjRef removedORI : removedORIs)
				{
					auditPropertyItem(removedORI, relationProperty, AuditedEntityPropertyItemChangeType.REMOVE, auditControllerState, objRefToRefMap);
				}
			}
		}
	}

	protected void auditPropertyItem(IObjRef objRef, CreateOrUpdateContainerBuild relationProperty, AuditedEntityPropertyItemChangeType changeType,
			AuditControllerState auditControllerState, IMap<IObjRef, IDirectObjRef> objRefToRefMap)
	{
		CreateOrUpdateContainerBuild propertyItem = auditControllerState.createEntity(IAuditedEntityRelationPropertyItem.class);
		RelationUpdateItemBuild items = relationProperty.ensureRelation(IAuditedEntityRelationProperty.Items);
		items.addObjRef(propertyItem.getReference());

		propertyItem.ensureRelation(IAuditedEntityRelationPropertyItem.Ref).addObjRef(getOrCreateRef(objRef, auditControllerState, objRefToRefMap));
		propertyItem.ensurePrimitive(IAuditedEntityRelationPropertyItem.ChangeType).setNewValue(changeType);
		propertyItem.ensurePrimitive(IAuditedEntityRelationPropertyItem.Order).setNewValue(Integer.valueOf(items.getAddedCount()));
	}

	@Override
	public void handlePostBegin(long sessionId) throws Throwable
	{
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		if (authorization != null)
		{
			AdditionalAuditInfo additionalAuditInfo = getAdditionalAuditInfo();
			additionalAuditInfo.clearTextPassword = context.getAuthentication().getPassword();
			additionalAuditInfo.doClearPassword = true;
		}
	}

	@Override
	public void handlePostRollback(long sessionId) throws Throwable
	{
		auditEntryTL.set(null);
		AdditionalAuditInfo additionalAuditInfo = additionalAuditInfoTL.get();
		if (additionalAuditInfo != null && additionalAuditInfo.doClearPassword)
		{
			additionalAuditInfo.clearTextPassword = null;
		}
	}

	@Override
	public void handlePreCommit(long sessionId) throws Throwable
	{
		final AuditControllerState auditEntryState = auditEntryTL.get();
		if (auditEntryState == null)
		{
			handlePostRollback(sessionId);
			return;
		}
		auditEntryTL.set(null);

		AdditionalAuditInfo additionalAuditInfo = getAdditionalAuditInfo();
		additionalAuditInfo.ownAuditMergeActive = Boolean.TRUE;
		try
		{
			final char[] clearTextPassword = additionalAuditInfo.clearTextPassword;

			securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					ArrayList<CreateOrUpdateContainerBuild> auditedChanges = auditEntryState.auditedChanges;
					CreateOrUpdateContainerBuild auditEntryContainer = auditedChanges.get(0);

					auditEntryToSignature.signAuditEntry(auditEntryContainer, clearTextPassword, auditEntryState.getSignatureOfUser());

					ArrayList<IChangeContainer> finalizedAuditChanges = new ArrayList<IChangeContainer>(auditedChanges.size());
					for (int a = 0, size = auditedChanges.size(); a < size; a++)
					{
						CreateOrUpdateContainerBuild createOrUpdate = auditedChanges.get(a);
						if (!createOrUpdate.isCreate())
						{
							throw new IllegalStateException();
						}
						ICreateOrUpdateContainer cc = createOrUpdate.build();
						((IDirectObjRef) cc.getReference()).setDirect(cc);
						IRelationUpdateItem[] ruis = cc.getFullRUIs();
						if (ruis != null)
						{
							for (int b = ruis.length; b-- > 0;)
							{
								IRelationUpdateItem rui = ruis[b];
								if (rui instanceof RelationUpdateItemBuild)
								{
									ruis[b] = ((RelationUpdateItemBuild) rui).buildRUI();
								}
							}
						}
						finalizedAuditChanges.add(cc);
					}
					CUDResult auditMerge = new CUDResult(finalizedAuditChanges, new ArrayList<Object>(new Object[auditedChanges.size()]));

					Boolean oldAddNewlyPersistedEntities = MergeProcess.getAddNewlyPersistedEntities();
					MergeProcess.setAddNewlyPersistedEntities(Boolean.FALSE);
					try
					{
						mergeService.merge(auditMerge, null);
					}
					finally
					{
						MergeProcess.setAddNewlyPersistedEntities(oldAddNewlyPersistedEntities);
					}

					return null;
				}
			});
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			additionalAuditInfo.ownAuditMergeActive = null;
			if (additionalAuditInfo.doClearPassword)
			{
				additionalAuditInfo.clearTextPassword = null;
			}
		}
	}

	public AdditionalAuditInfo getAdditionalAuditInfo()
	{
		AdditionalAuditInfo additionalAuditInfo = additionalAuditInfoTL.get();
		if (additionalAuditInfo == null)
		{
			additionalAuditInfo = new AdditionalAuditInfo();
			additionalAuditInfoTL.set(additionalAuditInfo);
		}
		return additionalAuditInfo;
	}

	@Override
	public void removeAuditInfo()
	{
		additionalAuditInfoTL.set(null);
	}

	@Override
	public void pushAuditReason(String auditReason)
	{
		getAdditionalAuditInfo().auditReasonContainer.add(auditReason);
	}

	@Override
	public String popAuditReason()
	{
		return getAdditionalAuditInfo().auditReasonContainer.popLastElement();
	}

	@Override
	public String peekAuditReason()
	{
		return getAdditionalAuditInfo().auditReasonContainer.peek();
	}

	@Override
	public void pushAuditContext(String auditContext)
	{
		getAdditionalAuditInfo().auditContextContainer.add(auditContext);
	}

	@Override
	public String popAuditContext()
	{
		return getAdditionalAuditInfo().auditContextContainer.popLastElement();
	}

	@Override
	public String peekAuditContext()
	{
		return getAdditionalAuditInfo().auditContextContainer.peek();
	}

	@Override
	public IAuditInfoRevert pushClearTextPassword(final char[] clearTextPassword)
	{
		final AdditionalAuditInfo additionalAuditInfo = getAdditionalAuditInfo();
		final char[] oldClearTextPassword = additionalAuditInfo.clearTextPassword;
		final boolean oldDoClearPassword = additionalAuditInfo.doClearPassword;
		additionalAuditInfo.clearTextPassword = clearTextPassword;
		additionalAuditInfo.doClearPassword = false;
		return new IAuditInfoRevert()
		{
			@Override
			public void revert()
			{
				if (additionalAuditInfo.clearTextPassword != clearTextPassword)
				{
					throw new IllegalStateException("Illegal state: clearTextPassword does not match");
				}
				additionalAuditInfo.clearTextPassword = oldClearTextPassword;
				additionalAuditInfo.doClearPassword = oldDoClearPassword;
			}
		};
	}

	@Override
	public IAuditInfoRevert setAuthorizedUser(final IUser user, final char[] clearTextPassword)
	{
		final String oldAuthorizedUserSID = authorizedUserHolder.getAuthorizedUserSID();
		final AdditionalAuditInfo additionalAuditInfo = getAdditionalAuditInfo();
		final IUser oldAuthorizedUser = additionalAuditInfo.authorizedUser;
		final char[] oldClearTextPassword = additionalAuditInfo.clearTextPassword;
		final String sid = userIdentifierProvider.getSID(user);

		additionalAuditInfo.authorizedUser = user;
		additionalAuditInfo.clearTextPassword = clearTextPassword;
		additionalAuditInfo.doClearPassword = false;
		authorizedUserHolder.setAuthorizedUserSID(sid);
		return new IAuditInfoRevert()
		{
			@Override
			public void revert()
			{
				if (authorizedUserHolder.getAuthorizedUserSID() != sid)
				{
					throw new IllegalStateException("Illegal state: authorizedUserSID does not match");
				}
				authorizedUserHolder.setAuthorizedUserSID(oldAuthorizedUserSID);

				if (additionalAuditInfo.authorizedUser != user)
				{
					throw new IllegalStateException("Illegal state: user does not match");
				}
				if (additionalAuditInfo.clearTextPassword != clearTextPassword)
				{
					throw new IllegalStateException("Illegal state: clearTextPassword does not match");
				}
				additionalAuditInfo.clearTextPassword = oldClearTextPassword;
				additionalAuditInfo.authorizedUser = oldAuthorizedUser;
			}
		};
	}
}
