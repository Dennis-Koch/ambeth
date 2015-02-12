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
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeListener;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.ProceedWithMergeHook;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
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
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ParamHolder;

public class AuditController implements IThreadLocalCleanupBean, IMethodCallLogger, IMergeListener, IAuditEntryVerifier, ITransactionListener, IStartingBean,
		IAuditEntryWriterExtendable, IAuditReasonController
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditConfigurationProvider auditConfigurationProvider;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IFirstLevelCacheManager firstLevelCacheManager;

	@Autowired
	protected IMergeProcess mergeProcess;

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
	protected final ThreadLocal<IAuditEntry> auditEntryTL = new ThreadLocal<IAuditEntry>();

	@Forkable
	protected final ThreadLocal<Boolean> ownAuditMergeActiveTL = new ThreadLocal<Boolean>();

	@Forkable
	protected final ThreadLocal<char[]> clearTextPasswordTL = new ThreadLocal<char[]>();

	protected IPrefetchHandle prefetchAuditEntries;

	@Forkable
	private final ThreadLocal<String> auditReasonTL = new ThreadLocal<String>();

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
		if (auditEntryTL.get() != null)
		{
			throw new IllegalStateException("Should never contain a value at this point");
		}
	}

	protected IAuditEntry ensureAuditEntry()
	{
		IAuditEntry auditEntry = auditEntryTL.get();
		if (auditEntry != null)
		{
			return auditEntry;
		}

		try
		{
			auditEntry = entityFactory.createEntity(IAuditEntry.class);

			Long currentTime = database.getContextProvider().getCurrentTime();
			auditEntry.setTimestamp(currentTime.longValue());
			auditEntry.setProtocol(protocol);

			ISecurityContext context = securityContextHolder.getContext();
			IAuthorization authorization = context != null ? context.getAuthorization() : null;
			if (authorization != null)
			{
				clearTextPasswordTL.set(context.getAuthentication().getPassword());
				final String currentSID = authorization.getSID();
				IUser currentUser = securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<IUser>()
				{
					@Override
					public IUser invoke() throws Throwable
					{
						return userResolver.resolveUserBySID(currentSID);
					}
				});
				auditEntry.setUser(currentUser);
			}
			auditEntryTL.set(auditEntry);
			return auditEntry;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IMethodCallHandle logMethodCallStart(Method method, Object[] args)
	{
		IAuditEntry auditEntry = ensureAuditEntry();

		List<IAuditedService> services = (List<IAuditedService>) auditEntry.getServices();
		IAuditedService auditedService = entityFactory.createEntity(IAuditedService.class);
		auditedService.setOrder(services.size() + 1);

		services.add(auditedService);

		auditedService.setServiceType(method.getDeclaringClass().getName());
		auditedService.setMethodName(method.getName());

		auditedService.setArguments(conversionHelper.convertValueToType(String[].class, args));

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
		handle.auditedService.setSpentTime(System.currentTimeMillis() - handle.start);
	}

	@Override
	public ICUDResult preMerge(ICUDResult cudResult, ICache cache)
	{
		// intended blank
		return cudResult;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs)
	{
		try
		{
			if (Boolean.TRUE.equals(ownAuditMergeActiveTL.get()))
			{
				// ignore this dataChange because it is our own Audit merge
				return;
			}
			ArrayList<IAuditedEntity> entities = new ArrayList<IAuditedEntity>(cudResult.getAllChanges().size());

			List<Object> originalRefs = cudResult.getOriginalRefs();
			List<IChangeContainer> allChanges = cudResult.getAllChanges();
			for (int index = allChanges.size(); index-- > 0;)
			{
				IChangeContainer changeContainer = allChanges.get(index);
				IObjRef updatedObjRef = updatedObjRefs[index];
				Object originalRef = originalRefs.get(index);
				auditChangeContainer(originalRef, updatedObjRef, changeContainer, entities);

				// IObjRef objRef = changeContainer.getReference();
				// IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
				// Audited audited = metaData.getEnhancedType().getAnnotation(Audited.class);
				// boolean auditEntity = audited != null ? audited.value() : auditedEntityDefaultModeActive;
				//
				// if (!auditEntity)
				// {
				// continue;
				// }
			}
			if (entities.size() > 0)
			{
				for (int a = entities.size(); a-- > 0;)
				{
					entities.get(a).setOrder(a + 1);
				}

				IAuditEntry auditEntry = ensureAuditEntry();
				auditEntry.setReason(auditReasonTL.get());
				((Collection<IAuditedEntity>) auditEntry.getEntities()).addAll(entities);
			}
		}
		finally
		{
			auditReasonTL.remove();
		}
	}

	protected void auditChangeContainer(Object originalRef, IObjRef updatedObjRef, IChangeContainer changeContainer, List<IAuditedEntity> auditedEntities)
	{
		IObjRef objRef = changeContainer.getReference();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
		IAuditConfiguration auditConfiguration = auditConfigurationProvider.getAuditConfiguration(metaData.getEntityType());
		if (auditConfiguration == null)
		{
			return;
		}

		// test if audit reason is required and throw exception if its not set
		if (auditConfiguration.isReasonRequired() && auditReasonTL.get() == null)
		{
			throw new AuditReasonMissingException("Audit reason is missing for " + originalRef.getClass() + "!");
		}

		IAuditedEntity auditedEntity = entityFactory.createEntity(IAuditedEntity.class);

		if (changeContainer instanceof CreateContainer)
		{
			auditedEntity.setChangeType(AuditedEntityChangeType.INSERT);
			auditedEntity.setEntityId(updatedObjRef.getId());
			auditedEntity.setEntityVersion(updatedObjRef.getVersion());
			auditPUIs(((CreateContainer) changeContainer).getPrimitives(), auditedEntity, metaData, auditConfiguration);
			auditRUIs(((CreateContainer) changeContainer).getRelations(), auditedEntity, metaData, auditConfiguration);
		}
		else if (changeContainer instanceof UpdateContainer)
		{
			auditedEntity.setChangeType(AuditedEntityChangeType.UPDATE);
			auditedEntity.setEntityId(updatedObjRef.getId());
			auditedEntity.setEntityVersion(updatedObjRef.getVersion());
			auditPUIs(((UpdateContainer) changeContainer).getPrimitives(), auditedEntity, metaData, auditConfiguration);
			auditRUIs(((UpdateContainer) changeContainer).getRelations(), auditedEntity, metaData, auditConfiguration);
		}
		else if (changeContainer instanceof DeleteContainer)
		{
			auditedEntity.setChangeType(AuditedEntityChangeType.DELETE);
			auditedEntity.setEntityId(objRef.getId());
			auditedEntity.setEntityVersion(objRef.getVersion());
		}

		auditedEntity.setEntityType(objRef.getRealType());

		auditedEntities.add(auditedEntity);
	}

	@SuppressWarnings("unchecked")
	protected void auditPUIs(IPrimitiveUpdateItem[] puis, IAuditedEntity auditedEntity, IEntityMetaData metaData, IAuditConfiguration auditConfiguration)
	{
		if (puis == null)
		{
			return;
		}
		List<IAuditedEntityPrimitiveProperty> properties = (List<IAuditedEntityPrimitiveProperty>) auditedEntity.getPrimitives();
		for (IPrimitiveUpdateItem pui : puis)
		{
			if (!auditConfiguration.getMemberConfiguration(pui.getMemberName()).isAuditActive())
			{
				continue;
			}
			IAuditedEntityPrimitiveProperty property = entityFactory.createEntity(IAuditedEntityPrimitiveProperty.class);
			property.setName(pui.getMemberName());
			property.setNewValue(pui.getNewValue());
			property.setOrder(properties.size() + 1);

			properties.add(property);
		}
	}

	@SuppressWarnings("unchecked")
	protected void auditRUIs(IRelationUpdateItem[] ruis, IAuditedEntity auditedEntity, IEntityMetaData metaData, IAuditConfiguration auditConfiguration)
	{
		if (ruis == null)
		{
			return;
		}
		List<IAuditedEntityRelationProperty> properties = (List<IAuditedEntityRelationProperty>) auditedEntity.getRelations();
		for (IRelationUpdateItem rui : ruis)
		{
			if (!auditConfiguration.getMemberConfiguration(rui.getMemberName()).isAuditActive())
			{
				continue;
			}

			IAuditedEntityRelationProperty property = entityFactory.createEntity(IAuditedEntityRelationProperty.class);
			property.setName(rui.getMemberName());

			List<IAuditedEntityRelationPropertyItem> items = (List<IAuditedEntityRelationPropertyItem>) property.getItems();

			IObjRef[] addedORIs = rui.getAddedORIs();
			if (addedORIs != null)
			{
				for (IObjRef addedORI : addedORIs)
				{
					auditPropertyItem(addedORI, items, AuditedEntityPropertyItemChangeType.ADD);
				}
			}
			IObjRef[] removedORIs = rui.getRemovedORIs();
			if (removedORIs != null)
			{
				for (IObjRef removedORI : removedORIs)
				{
					auditPropertyItem(removedORI, items, AuditedEntityPropertyItemChangeType.REMOVE);
				}
			}
			property.setOrder(properties.size() + 1);

			properties.add(property);
		}
	}

	protected void auditPropertyItem(IObjRef objRef, List<IAuditedEntityRelationPropertyItem> propertyItems, AuditedEntityPropertyItemChangeType changeType)
	{
		IAuditedEntityRelationPropertyItem propertyItem = entityFactory.createEntity(IAuditedEntityRelationPropertyItem.class);

		propertyItem.setEntityId(objRef.getId());
		propertyItem.setEntityType(objRef.getRealType());
		propertyItem.setEntityVersion(objRef.getVersion());
		propertyItem.setChangeType(changeType);
		propertyItem.setOrder(propertyItems.size() + 1);

		propertyItems.add(propertyItem);
	}

	protected void signAuditEntry(IAuditEntry auditEntry)
	{
		IUser user = auditEntry.getUser();
		char[] clearTextPassword = clearTextPasswordTL.get();

		java.security.Signature signatureHandle = privateKeyProvider.getSigningHandle(user, clearTextPassword);
		auditEntry.setUserIdentifier(user != null ? userIdentifierProvider.getSID(user) : null);
		auditEntry.setSignatureOfUser(user != null ? user.getSignature() : null);
		if (signatureHandle == null)
		{
			auditEntry.setSignature(null);
			auditEntry.setHashAlgorithm(null);
			auditEntry.setSignature(null);
			;
			return;
		}
		try
		{
			auditEntry.setHashAlgorithm(hashAlgorithm);
			auditEntry.setProtocol(protocol);

			writeToSignatureHandle(signatureHandle, auditEntry);

			byte[] sign = signatureHandle.sign();

			auditEntry.setSignature(Base64.encodeBytes(sign).toCharArray());
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
				writeToSignatureHandle(signatureHandle, auditEntry);
				allEntriesValid |= signatureHandle.verify(Base64.decode(auditEntry.getSignature()));
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return allEntriesValid;
	}

	protected void writeToSignatureHandle(java.security.Signature signatureHandle, IAuditEntry auditEntry)
	{
		try
		{
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(Integer.valueOf(auditEntry.getProtocol()));
			if (auditEntryWriter == null)
			{
				throw new IllegalArgumentException("Not instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '"
						+ auditEntry.getProtocol() + "' of " + auditEntry);
			}
			String hashAlgorithm = auditEntry.getHashAlgorithm();
			if (hashAlgorithm != null && hashAlgorithm.length() > 0)
			{
				// build a good hash from the audited information: to sign its hash is faster than to sign the audited information itself
				// the clue is to choose a good hash algorithm which is fast enough to make sense but much stronger than e.g. MD5 as well...

				MessageDigest md = MessageDigest.getInstance(hashAlgorithm);

				DigestOutputStream digestOS = new DigestOutputStream(new NullOutputStream(), md);
				DataOutputStream dos = new DataOutputStream(digestOS);

				auditEntryWriter.writeAuditEntry(auditEntry, dos);
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
		// intended blank
	}

	@Override
	public void handlePostRollback(long sessionId) throws Throwable
	{
		auditEntryTL.remove();
		clearTextPasswordTL.remove();
	}

	@Override
	public void handlePreCommit(long sessionId) throws Throwable
	{
		final IAuditEntry auditEntry = auditEntryTL.get();
		if (auditEntry == null)
		{
			return;
		}
		auditEntryTL.remove();

		ownAuditMergeActiveTL.set(Boolean.TRUE);
		try
		{
			final ParamHolder<ICUDResult> cudResultHolder = new ParamHolder<ICUDResult>();
			securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					signAuditEntry(auditEntry);
					mergeProcess.process(auditEntry, null, new ProceedWithMergeHook()
					{
						@Override
						public boolean checkToProceed(ICUDResult result)
						{
							cudResultHolder.setValue(result);
							return true;
						}
					}, null, false);
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
			ownAuditMergeActiveTL.remove();
			clearTextPasswordTL.remove();
		}
	}

	@Override
	public void setAuditReason(String auditReason)
	{
		auditReasonTL.set(auditReason);
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
