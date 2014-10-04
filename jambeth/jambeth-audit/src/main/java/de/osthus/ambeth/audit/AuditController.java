package de.osthus.ambeth.audit;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import de.osthus.ambeth.cache.IFirstLevelCacheManager;
import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransactionListener;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
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
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISignatureUtil;
import de.osthus.ambeth.security.IUserResolver;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ParamHolder;

public class AuditController implements IThreadLocalCleanupBean, IMethodCallLogger, IMergeListener, IAuditEntryVerifier, ITransactionListener, IStartingBean,
		IInitializingBean
{
	private static final Charset utf8 = Charset.forName("UTF-8");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditEntryFactory auditEntryFactory;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IFirstLevelCacheManager firstLevelCacheManager;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired(optional = true)
	protected IUserResolver userResolver;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Property(name = AuditConfigurationConstants.AuditedInformationHashAlgorithm, defaultValue = "SHA-256")
	protected String hashAlgorithm;

	@Property(name = AuditConfigurationConstants.AuditedServiceDefaultModeActive, defaultValue = "true")
	protected boolean auditedServiceDefaultModeActive;

	@Property(name = AuditConfigurationConstants.AuditedEntityDefaultModeActive, defaultValue = "true")
	protected boolean auditedEntityDefaultModeActive;

	@Property(name = AuditConfigurationConstants.AuditedEntityPropertyDefaultModeActive, defaultValue = "true")
	protected boolean auditedEntityPropertyDefaultModeActive;

	protected final ThreadLocal<IAuditEntry> auditEntryTL = new ThreadLocal<IAuditEntry>();

	protected final ThreadLocal<Boolean> ownAuditMergeActiveTL = new ThreadLocal<Boolean>();

	protected IPrefetchHandle prefetchAuditEntries;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		System.out.println("DFDF");
	}

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
			auditEntry = auditEntryFactory.createAuditEntry();

			Long currentTime = database.getContextProvider().getCurrentTime();
			auditEntry.setTimestamp(currentTime.longValue());

			if (userResolver != null)
			{
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
					auditEntry.setUser(currentUser);
				}
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
	public IMethodCallHandle logMethodCallStart(Method method)
	{
		Audited audited = method.getAnnotation(Audited.class);
		if (audited == null)
		{
			audited = method.getDeclaringClass().getAnnotation(Audited.class);
		}
		boolean auditMethod = audited != null ? audited.value() : auditedServiceDefaultModeActive;
		if (!auditMethod)
		{
			// do not audit this specific method
			return null;
		}
		IAuditEntry auditEntry = ensureAuditEntry();

		List<IAuditedService> services = (List<IAuditedService>) auditEntry.getServices();
		IAuditedService auditedService = auditEntryFactory.createAuditedService();
		auditedService.setOrder(services.size() + 1);

		services.add(auditedService);

		auditedService.setServiceType(method.getDeclaringClass().getName());
		auditedService.setMethodName(method.getName());

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

	/**
	 * This method gets called DIRECTLY via the event API
	 * 
	 * @param evnt
	 */
	@Override
	public void preMerge(ICUDResult cudResult)
	{
		// intended blank
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postMerge(ICUDResult cudResult)
	{
		if (Boolean.TRUE.equals(ownAuditMergeActiveTL.get()))
		{
			// ignore this dataChange because it is our own Audit merge
			return;
		}
		IAuditEntry auditEntry = null;
		List<IAuditedEntity> entities = null;

		for (IChangeContainer changeContainer : cudResult.getAllChanges())
		{
			IObjRef objRef = changeContainer.getReference();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
			Audited audited = metaData.getEnhancedType().getAnnotation(Audited.class);
			boolean auditEntity = audited != null ? audited.value() : auditedEntityDefaultModeActive;

			if (!auditEntity)
			{
				continue;
			}
			IAuditedEntity auditedEntity = auditEntryFactory.createAuditedEntity();

			if (changeContainer instanceof CreateContainer)
			{
				auditedEntity.setChangeType(AuditedEntityChangeType.INSERT);
				auditPUIs(((CreateContainer) changeContainer).getPrimitives(), auditedEntity, metaData);
				auditRUIs(((CreateContainer) changeContainer).getRelations(), auditedEntity, metaData);
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				auditedEntity.setChangeType(AuditedEntityChangeType.UPDATE);
				auditPUIs(((UpdateContainer) changeContainer).getPrimitives(), auditedEntity, metaData);
				auditRUIs(((UpdateContainer) changeContainer).getRelations(), auditedEntity, metaData);
			}
			else if (changeContainer instanceof DeleteContainer)
			{
				auditedEntity.setChangeType(AuditedEntityChangeType.DELETE);
			}
			auditedEntity.setEntityType(objRef.getRealType());
			auditedEntity.setEntityId(objRef.getId());
			auditedEntity.setEntityVersion(objRef.getVersion());

			if (auditEntry == null)
			{
				auditEntry = ensureAuditEntry();
				entities = (List<IAuditedEntity>) auditEntry.getEntities();
			}
			auditedEntity.setOrder(entities.size() + 1);

			entities.add(auditedEntity);
		}
	}

	@SuppressWarnings("unchecked")
	protected void auditPUIs(IPrimitiveUpdateItem[] puis, IAuditedEntity auditedEntity, IEntityMetaData metaData)
	{
		if (puis == null)
		{
			return;
		}
		List<IAuditedEntityPrimitiveProperty> properties = (List<IAuditedEntityPrimitiveProperty>) auditedEntity.getPrimitives();
		for (IPrimitiveUpdateItem pui : puis)
		{
			Member member = metaData.getMemberByName(pui.getMemberName());
			Audited audited = member.getAnnotation(Audited.class);
			boolean auditMember = audited != null ? audited.value() : auditedEntityPropertyDefaultModeActive;

			if (!auditMember)
			{
				continue;
			}
			IAuditedEntityPrimitiveProperty property = auditEntryFactory.createAuditedEntityPrimitiveProperty();
			property.setName(pui.getMemberName());
			property.setNewValue(pui.getNewValue());
			property.setOrder(properties.size() + 1);

			properties.add(property);
		}
	}

	@SuppressWarnings("unchecked")
	protected void auditRUIs(IRelationUpdateItem[] ruis, IAuditedEntity auditedEntity, IEntityMetaData metaData)
	{
		if (ruis == null)
		{
			return;
		}
		List<IAuditedEntityRelationProperty> properties = (List<IAuditedEntityRelationProperty>) auditedEntity.getRelations();
		for (IRelationUpdateItem rui : ruis)
		{
			Member member = metaData.getMemberByName(rui.getMemberName());
			Audited audited = member.getAnnotation(Audited.class);
			boolean auditMember = audited != null ? audited.value() : auditedEntityPropertyDefaultModeActive;

			if (!auditMember)
			{
				continue;
			}
			IAuditedEntityRelationProperty property = auditEntryFactory.createAuditedEntityRelationProperty();
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
		IAuditedEntityRelationPropertyItem propertyItem = auditEntryFactory.createAuditedEntityRelationPropertyItem();

		propertyItem.setEntityId(objRef.getId());
		propertyItem.setEntityType(objRef.getRealType());
		propertyItem.setEntityVersion(objRef.getVersion());
		propertyItem.setChangeType(changeType);
		propertyItem.setOrder(propertyItems.size() + 1);

		propertyItems.add(propertyItem);
	}

	protected void signAuditEntry(IAuditEntry auditEntry)
	{
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		if (authorization == null)
		{
			return;
		}
		IAuthentication authentication = context.getAuthentication();
		char[] clearTextPassword = authentication.getPassword();
		// the signatureUtil expects the "clearTextPassword" base64-encoded
		clearTextPassword = Base64.encodeBytes(new String(clearTextPassword).getBytes(utf8)).toCharArray();
		IUser user = auditEntry.getUser();
		ISignature signature = user.getSignature();
		if (signature == null)
		{
			signature = auditEntryFactory.createSignature();
			signatureUtil.updateSignature(signature, clearTextPassword, user);
		}
		try
		{
			auditEntry.setHashAlgorithm(hashAlgorithm);

			java.security.Signature signatureHandle = signatureUtil.createSignatureHandle(signature, clearTextPassword);
			updateSignatureHandle(signatureHandle, auditEntry);

			byte[] sign = signatureHandle.sign();

			auditEntry.setSignature(Base64.encodeBytes(sign).toCharArray());

			boolean result = verifyAuditEntries(Arrays.asList(auditEntry));
			System.out.println(result);
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
		HashMap<IUser, java.security.Signature> userToSignatureMap = new HashMap<IUser, java.security.Signature>();
		for (IAuditEntry auditEntry : auditEntries)
		{
			IUser user = auditEntry.getUser();
			try
			{
				java.security.Signature signatureHandle = userToSignatureMap.get(user);
				if (signatureHandle == null)
				{
					signatureHandle = signatureUtil.createVerifyHandle(user.getSignature());
					userToSignatureMap.put(user, signatureHandle);
				}
				updateSignatureHandle(signatureHandle, auditEntry);
				allEntriesValid |= signatureHandle.verify(Base64.decode(auditEntry.getSignature()));
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return allEntriesValid;
	}

	protected void updateSignatureHandle(java.security.Signature signatureHandle, IAuditEntry auditEntry)
	{
		try
		{
			String hashAlgorithm = auditEntry.getHashAlgorithm();
			if (hashAlgorithm != null && hashAlgorithm.length() > 0)
			{
				// build a good hash from the audited information: to sign the hash later is faster than to sign the audited information itself
				// the clue is to choose a good hash algorithm with is fast enough to make sense but much stronger than e.g. MD5...

				MessageDigest md = MessageDigest.getInstance(hashAlgorithm);

				DigestOutputStream digestOS = new DigestOutputStream(new NullOutputStream(), md);
				DataOutputStream dos = new DataOutputStream(digestOS);
				writeAuditEntry(auditEntry, dos);
				dos.close();

				byte[] digestToSign = md.digest();
				signatureHandle.update(digestToSign);
			}
			else
			{
				// we have no hashAlgorithm: so we sign the whole audited information
				SignatureOutputStream sos = new SignatureOutputStream(new NullOutputStream(), signatureHandle);
				DataOutputStream dos = new DataOutputStream(sos);
				writeAuditEntry(auditEntry, dos);
				dos.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void writeAuditEntry(IAuditEntry auditEntry, DataOutputStream os)
	{
		writeProperty(IAuditEntry.User, auditEntry.getUser().getAuditedIdentifier(), os);
		for (IAuditedService auditedService : sortAuditServices(auditEntry))
		{
			writeProperty(IAuditedService.ServiceType, auditedService.getServiceType(), os);
			writeProperty(IAuditedService.MethodName, auditedService.getMethodName(), os);
			writeProperty(IAuditedService.SpentTime, auditedService.getSpentTime(), os);
		}
		for (IAuditedEntity auditedEntity : sortAuditEntities(auditEntry))
		{
			writeProperty(IAuditedEntity.EntityType, auditedEntity.getEntityType(), os);
			writeProperty(IAuditedEntity.EntityId, auditedEntity.getEntityId(), os);
			writeProperty(IAuditedEntity.EntityVersion, auditedEntity.getEntityVersion(), os);
			writeProperty(IAuditedEntity.ChangeType, auditedEntity.getChangeType(), os);

			for (IAuditedEntityPrimitiveProperty property : sortAuditedEntityPrimitives(auditedEntity))
			{
				writeProperty(IAuditedEntityPrimitiveProperty.Name, property.getName(), os);
				writeProperty(IAuditedEntityPrimitiveProperty.NewValue, property.getNewValue(), os);
			}
			for (IAuditedEntityRelationProperty property : sortAuditedEntityRelations(auditedEntity))
			{
				writeProperty(IAuditedEntityRelationProperty.Name, property.getName(), os);

				for (IAuditedEntityRelationPropertyItem item : sortAuditedEntityRelationItems(property))
				{
					writeProperty(IAuditedEntityRelationPropertyItem.EntityType, item.getEntityType(), os);
					writeProperty(IAuditedEntityRelationPropertyItem.EntityId, item.getEntityId(), os);
					writeProperty(IAuditedEntityRelationPropertyItem.EntityVersion, item.getEntityVersion(), os);
					writeProperty(IAuditedEntityRelationPropertyItem.ChangeType, item.getChangeType(), os);
				}
			}
		}
	}

	protected void writeProperty(String name, Object value, DataOutputStream os)
	{
		try
		{
			os.writeUTF(name);
			if (value == null)
			{
				os.writeBoolean(false);
			}
			else
			{
				os.writeBoolean(true);
				os.writeUTF(value.toString());
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected List<IAuditedEntity> sortAuditEntities(IAuditEntry auditEntry)
	{
		ArrayList<IAuditedEntity> entities = new ArrayList<IAuditedEntity>(auditEntry.getEntities());

		Collections.sort(entities, new Comparator<IAuditedEntity>()
		{
			@Override
			public int compare(IAuditedEntity o1, IAuditedEntity o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return entities;
	}

	protected List<IAuditedService> sortAuditServices(IAuditEntry auditEntry)
	{
		ArrayList<IAuditedService> services = new ArrayList<IAuditedService>(auditEntry.getServices());

		Collections.sort(services, new Comparator<IAuditedService>()
		{
			@Override
			public int compare(IAuditedService o1, IAuditedService o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return services;
	}

	protected List<IAuditedEntityPrimitiveProperty> sortAuditedEntityPrimitives(IAuditedEntity auditedEntity)
	{
		ArrayList<IAuditedEntityPrimitiveProperty> properties = new ArrayList<IAuditedEntityPrimitiveProperty>(auditedEntity.getPrimitives());

		Collections.sort(properties, new Comparator<IAuditedEntityPrimitiveProperty>()
		{
			@Override
			public int compare(IAuditedEntityPrimitiveProperty o1, IAuditedEntityPrimitiveProperty o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return properties;
	}

	protected List<IAuditedEntityRelationProperty> sortAuditedEntityRelations(IAuditedEntity auditedEntity)
	{
		ArrayList<IAuditedEntityRelationProperty> properties = new ArrayList<IAuditedEntityRelationProperty>(auditedEntity.getRelations());

		Collections.sort(properties, new Comparator<IAuditedEntityRelationProperty>()
		{
			@Override
			public int compare(IAuditedEntityRelationProperty o1, IAuditedEntityRelationProperty o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return properties;
	}

	protected List<IAuditedEntityRelationPropertyItem> sortAuditedEntityRelationItems(IAuditedEntityRelationProperty property)
	{
		ArrayList<IAuditedEntityRelationPropertyItem> items = new ArrayList<IAuditedEntityRelationPropertyItem>(property.getItems());

		Collections.sort(items, new Comparator<IAuditedEntityRelationPropertyItem>()
		{
			@Override
			public int compare(IAuditedEntityRelationPropertyItem o1, IAuditedEntityRelationPropertyItem o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return items;
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

		signAuditEntry(auditEntry);

		ownAuditMergeActiveTL.set(Boolean.TRUE);
		try
		{
			final ParamHolder<ICUDResult> cudResultHolder = new ParamHolder<ICUDResult>();
			securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					mergeProcess.process(auditEntry, null, new ProceedWithMergeHook()
					{
						@Override
						public boolean checkToProceed(ICUDResult result)
						{
							cudResultHolder.setValue(result);
							return true;
						}
					}, null);
					return null;
				}
			});

			// remove our newly added items immediately from the cache to suppress being reloaded from the DB via the cache hierarchy
			// for performance reasons. This is due to the fact that we almost never need the created audit information later in cache
			IList<IWritableCache> selectFirstLevelCaches = firstLevelCacheManager.selectFirstLevelCaches();
			List<IChangeContainer> changes = cudResultHolder.getValue().getAllChanges();
			ArrayList<IObjRef> objRefList = new ArrayList<IObjRef>(changes.size());
			for (int a = changes.size(); a-- > 0;)
			{
				IObjRef objRef = changes.get(a).getReference();
				objRefList.add(objRef);
			}
			for (int a = selectFirstLevelCaches.size(); a-- > 0;)
			{
				IWritableCache firstLevelCache = selectFirstLevelCaches.get(a);
				firstLevelCache.remove(objRefList);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			ownAuditMergeActiveTL.remove();
		}
	}
}
