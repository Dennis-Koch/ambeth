package de.osthus.ambeth.audit;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Collection;
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
import de.osthus.ambeth.audit.util.NullOutputStream;
import de.osthus.ambeth.audit.util.SignatureOutputStream;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.IFirstLevelCacheManager;
import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransactionListener;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.exceptions.AuditReasonMissingException;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ICUDResultApplier;
import de.osthus.ambeth.merge.ICUDResultHelper;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeListener;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.IObjRefHelper;
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
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.IPrivateKeyProvider;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISignatureUtil;
import de.osthus.ambeth.security.IUserIdentifierProvider;
import de.osthus.ambeth.security.IUserResolver;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;

public class AuditController implements IThreadLocalCleanupBean, IMethodCallLogger, IMergeListener, IAuditEntryVerifier, ITransactionListener, IStartingBean,
		IAuditEntryWriterExtendable, IAuditInfoController
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditConfigurationProvider auditConfigurationProvider;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected ICUDResultApplier cudResultApplier;

	@Autowired
	protected ICUDResultHelper cudResultHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IFirstLevelCacheManager firstLevelCacheManager;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IMergeService mergeService;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected IPrivateKeyProvider privateKeyProvider;

	@Autowired
	protected IUserIdentifierProvider userIdentifierProvider;

	@Autowired
	protected IUserResolver userResolver;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Property(name = AuditConfigurationConstants.ProtocolVersion, defaultValue = "1")
	protected int protocol;

	@Property(name = AuditConfigurationConstants.AuditedInformationHashAlgorithm, defaultValue = "SHA-256")
	protected String hashAlgorithm;

	@Property(name = AuditConfigurationConstants.AuditedServiceDefaultModeActive, defaultValue = "true")
	protected boolean auditedServiceDefaultModeActive;

	protected final MapExtendableContainer<Integer, IAuditEntryWriter> auditEntryWriters = new MapExtendableContainer<Integer, IAuditEntryWriter>(
			"auditEntryWriter", "auditEntryProtocol");

	@Forkable
	private final ThreadLocal<AdditionalAuditInfo> additionalAuditInfoTL = new ThreadLocal<AdditionalAuditInfo>();

	@Forkable
	protected final ThreadLocal<AuditControllerState> auditEntryTL = new ThreadLocal<AuditControllerState>();

	protected IPrefetchHandle prefetchAuditEntries;

	@Override
	public void afterStarted() throws Throwable
	{
		prefetchAuditEntries = prefetchHelper.createPrefetch()//
				.add(IAuditEntry.class, IAuditEntry.Services)//
				.add(IAuditEntry.class, IAuditEntry.Entities)//
				.add(IAuditedEntity.class, IAuditedEntity.Primitives)//
				.add(IAuditedEntity.class, IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items).build();
	}

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
			auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(protocol);
			auditEntry.ensurePrimitive(IAuditEntry.Context).setNewValue(peekAuditContext());
			auditEntry.ensurePrimitive(IAuditEntry.Reason).setNewValue(peekAuditReason());

			ISecurityContext context = securityContextHolder.getContext();
			IAuthorization authorization = context != null ? context.getAuthorization() : null;
			if (authorization != null)
			{
				final String currentSID = authorization.getSID();
				IUser currentUser = securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<IUser>()
				{
					@Override
					public IUser invoke() throws Throwable
					{
						return userResolver.resolveUserBySID(currentSID);
					}
				});
				auditEntry.ensureRelation(IAuditEntry.User).addObjRef(objRefHelper.entityToObjRef(currentUser));
				auditEntry.ensurePrimitive(IAuditEntry.UserIdentifier).setNewValue(userIdentifierProvider.getSID(currentUser));

				ISignature signatureOfUser = currentUser.getSignature();
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
		auditedEntity.ensurePrimitive(IAuditedEntity.Order).setNewValue(entities.getAddedCount());

		if (changeContainer instanceof CreateContainer)
		{
			auditedEntity.ensurePrimitive(IAuditedEntity.ChangeType).setNewValue(AuditedEntityChangeType.INSERT);
			auditedEntity.ensureRelation(IAuditedEntity.Ref).addObjRef(getOrCreateRef(updatedObjRef, auditControllerState, objRefToRefMap));
			auditPUIs(((CreateContainer) changeContainer).getPrimitives(), auditedEntity, auditConfiguration, auditControllerState);
			auditRUIs(((CreateContainer) changeContainer).getRelations(), auditedEntity, auditConfiguration, auditControllerState, objRefToRefMap);
		}
		else if (changeContainer instanceof UpdateContainer)
		{
			auditedEntity.ensurePrimitive(IAuditedEntity.ChangeType).setNewValue(AuditedEntityChangeType.UPDATE);
			auditedEntity.ensureRelation(IAuditedEntity.Ref).addObjRef(getOrCreateRef(updatedObjRef, auditControllerState, objRefToRefMap));
			auditPUIs(((UpdateContainer) changeContainer).getPrimitives(), auditedEntity, auditConfiguration, auditControllerState);
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
			return ref;
		}
		CreateOrUpdateContainerBuild auditedEntityRef = auditControllerState.createEntity(IAuditedEntityRef.class);
		ref = (IDirectObjRef) auditedEntityRef.getReference();

		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityId).setNewValue(objRef.getId());
		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityType).setNewValue(objRef.getRealType());
		auditedEntityRef.ensurePrimitive(IAuditedEntityRef.EntityVersion).setNewValue(objRef.getVersion());

		objRefToRefMap.put(objRef, ref);
		return ref;
	}

	protected void auditPUIs(IPrimitiveUpdateItem[] puis, CreateOrUpdateContainerBuild auditedEntity, IAuditConfiguration auditConfiguration,
			AuditControllerState auditControllerState)
	{
		if (puis == null)
		{
			return;
		}
		for (IPrimitiveUpdateItem pui : puis)
		{
			if (!auditConfiguration.getMemberConfiguration(pui.getMemberName()).isAuditActive())
			{
				continue;
			}
			CreateOrUpdateContainerBuild primitiveProperty = auditControllerState.createEntity(IAuditedEntityPrimitiveProperty.class);
			RelationUpdateItemBuild primitives = auditedEntity.ensureRelation(IAuditedEntity.Primitives);
			primitives.addObjRef(primitiveProperty.getReference());

			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.Name).setNewValue(pui.getMemberName());
			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.NewValue).setNewValue(
					conversionHelper.convertValueToType(String.class, pui.getNewValue()));
			primitiveProperty.ensurePrimitive(IAuditedEntityPrimitiveProperty.Order).setNewValue(Integer.valueOf(primitives.getAddedCount()));
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

	protected void signAuditEntry(CreateOrUpdateContainerBuild auditEntry, char[] clearTextPassword, ISignature signature)
	{
		java.security.Signature signatureHandle = privateKeyProvider.getSigningHandle(signature, clearTextPassword);
		if (signatureHandle == null)
		{
			auditEntry.ensurePrimitive(IAuditEntry.HashAlgorithm).setNewValue(null);
			auditEntry.ensurePrimitive(IAuditEntry.Signature).setNewValue(null);
			auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(null);
			return;
		}
		try
		{
			auditEntry.ensurePrimitive(IAuditEntry.HashAlgorithm).setNewValue(hashAlgorithm);
			auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(protocol);

			if (signature != null)
			{
				auditEntry.ensureRelation(IAuditEntry.SignatureOfUser).addObjRef(objRefHelper.entityToObjRef(signature));
			}
			writeToSignatureHandle(signatureHandle, null, auditEntry);

			byte[] sign = signatureHandle.sign();

			auditEntry.ensurePrimitive(IAuditEntry.Signature).setNewValue(Base64.encodeBytes(sign).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean verifyAuditEntries(Collection<? extends IAuditEntry> auditEntries)
	{
		prefetchAuditEntries.prefetch(auditEntries);
		boolean allEntriesValid = true;
		HashMap<ISignature, java.security.Signature> signatureToSignatureHandleMap = new HashMap<ISignature, java.security.Signature>();
		for (IAuditEntry auditEntry : auditEntries)
		{
			ISignature signature = auditEntry.getSignatureOfUser();
			if (signature == null)
			{
				if (auditEntry.getSignature() == null)
				{
					// audit entries without a signature can not be verified but are intentionally treated as "valid"
					continue;
				}
				throw new IllegalArgumentException(IAuditEntry.class.getSimpleName() + " has no signature to verify: " + auditEntry);
			}
			try
			{
				java.security.Signature signatureHandle = signatureToSignatureHandleMap.get(signature);
				if (signatureHandle == null)
				{
					signatureHandle = signatureUtil.createVerifyHandle(signature.getSignAndVerify(), Base64.decode(signature.getPublicKey()));
					signatureToSignatureHandleMap.put(signature, signatureHandle);
				}
				writeToSignatureHandle(signatureHandle, auditEntry, null);
				allEntriesValid |= signatureHandle.verify(Base64.decode(auditEntry.getSignature()));
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return allEntriesValid;
	}

	protected int getProtocol(IAuditEntry auditEntry, CreateOrUpdateContainerBuild auditEntryContainer)
	{
		if (auditEntry != null)
		{
			return auditEntry.getProtocol();
		}
		return conversionHelper.convertValueToType(Integer.class, auditEntryContainer.findPrimitive(IAuditEntry.Protocol).getNewValue()).intValue();
	}

	protected String getHashAlgorithm(IAuditEntry auditEntry, CreateOrUpdateContainerBuild auditEntryContainer)
	{
		if (auditEntry != null)
		{
			return auditEntry.getHashAlgorithm();
		}
		return conversionHelper.convertValueToType(String.class, auditEntryContainer.findPrimitive(IAuditEntry.HashAlgorithm).getNewValue());
	}

	protected void writeToSignatureHandle(java.security.Signature signatureHandle, IAuditEntry auditEntry, CreateOrUpdateContainerBuild auditEntryContainer)
	{
		try
		{
			int protocol = getProtocol(auditEntry, auditEntryContainer);
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null)
			{
				throw new IllegalArgumentException("Not instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '" + protocol + "' of "
						+ auditEntry);
			}
			String hashAlgorithm = getHashAlgorithm(auditEntry, auditEntryContainer);
			if (hashAlgorithm != null && hashAlgorithm.length() > 0)
			{
				// build a good hash from the audited information: to sign its hash is faster than to sign the audited information itself
				// the clue is to choose a good hash algorithm which is fast enough to make sense but much stronger than e.g. MD5 as well...

				MessageDigest md = MessageDigest.getInstance(hashAlgorithm);

				DigestOutputStream digestOS = new DigestOutputStream(new NullOutputStream(), md);
				DataOutputStream dos = new DataOutputStream(digestOS);

				if (auditEntry != null)
				{
					auditEntryWriter.writeAuditEntry(auditEntry, dos);
				}
				else
				{
					auditEntryWriter.writeAuditEntry(auditEntryContainer, dos);
				}
				dos.close();

				byte[] digestToSign = md.digest();
				signatureHandle.update(digestToSign);
			}
			else
			{
				// we have no hashAlgorithm: so we sign the whole audited information
				SignatureOutputStream sos = new SignatureOutputStream(new NullOutputStream(), signatureHandle);
				DataOutputStream dos = new DataOutputStream(sos);
				auditEntryWriter.writeAuditEntry(auditEntry, dos);
				dos.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void handlePostBegin(long sessionId) throws Throwable
	{
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		if (authorization != null)
		{
			getAdditionalAuditInfo().clearTextPassword = context.getAuthentication().getPassword();
		}
	}

	@Override
	public void handlePostRollback(long sessionId) throws Throwable
	{
		auditEntryTL.set(null);
		getAdditionalAuditInfo().clearTextPassword = null;
	}

	@Override
	public void handlePreCommit(long sessionId) throws Throwable
	{
		final AuditControllerState auditEntryState = auditEntryTL.get();
		if (auditEntryState == null)
		{
			getAdditionalAuditInfo().clearTextPassword = null;
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

					signAuditEntry(auditEntryContainer, clearTextPassword, auditEntryState.getSignatureOfUser());

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
					mergeService.merge(auditMerge, null);

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
			additionalAuditInfo.clearTextPassword = null;
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
	public void registerAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion)
	{
		auditEntryWriters.register(auditEntryWriter, Integer.valueOf(protocolVersion));
	}

	@Override
	public void unregisterAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion)
	{
		auditEntryWriters.unregister(auditEntryWriter, Integer.valueOf(protocolVersion));
	}
}
