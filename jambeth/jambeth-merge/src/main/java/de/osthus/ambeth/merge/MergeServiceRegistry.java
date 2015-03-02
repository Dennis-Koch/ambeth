package de.osthus.ambeth.merge;

import java.util.List;
import java.util.Map.Entry;

import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.incremental.IncrementalMergeState;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.merge.transfer.CUDResult;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.OriCollection;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ParamHolder;

public class MergeServiceRegistry implements IMergeService, IMergeServiceExtensionExtendable, IMergeListenerExtendable, IMergeTimeProvider,
		IThreadLocalCleanupBean
{
	public static class MergeOperation
	{
		protected IMergeServiceExtension mergeServiceExtension;

		protected IList<IChangeContainer> changeContainer;

		public void setMergeServiceExtension(IMergeServiceExtension mergeServiceExtension)
		{
			this.mergeServiceExtension = mergeServiceExtension;
		}

		public IMergeServiceExtension getMergeServiceExtension()
		{
			return mergeServiceExtension;
		}

		public void setChangeContainer(IList<IChangeContainer> changeContainer)
		{
			this.changeContainer = changeContainer;
		}

		public IList<IChangeContainer> getChangeContainer()
		{
			return changeContainer;
		}
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ICUDResultApplier cudResultApplier;

	@Autowired
	protected ICUDResultComparer cudResultComparer;

	@Autowired(optional = true)
	protected ICUDResultPrinter cudResultPrinter;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IMergeController mergeController;

	@Autowired(optional = true)
	protected IMergeSecurityManager mergeSecurityManager;

	@Autowired(optional = true)
	protected ISecurityActivation securityActive;

	@Autowired(optional = true)
	protected ILightweightTransaction transaction;

	protected final ClassExtendableContainer<IMergeServiceExtension> mergeServiceExtensions = new ClassExtendableContainer<IMergeServiceExtension>(
			"mergeServiceExtension", "entityType");

	protected final DefaultExtendableContainer<IMergeListener> mergeListeners = new DefaultExtendableContainer<IMergeListener>(IMergeListener.class,
			"mergeListener");

	@Forkable
	protected final ThreadLocal<Long> startTimeTL = new ThreadLocal<Long>();

	@Override
	public void cleanupThreadLocal()
	{
		// intended blank. Interface is just needed to make the @Forkable annotation work
	}

	@Override
	public IOriCollection merge(final ICUDResult cudResult, final IMethodDescription methodDescription)
	{
		ParamChecker.assertParamNotNull(cudResult, "cudResult");
		Long startTime = startTimeTL.get();
		boolean startTimeHasBeenSet = false;
		if (startTime == null)
		{
			startTime = Long.valueOf(System.currentTimeMillis());
			startTimeTL.set(startTime);
			startTimeHasBeenSet = true;
		}
		try
		{
			if (transaction == null || transaction.isActive())
			{
				return mergeIntern(cudResult, methodDescription);
			}
			return transaction.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<IOriCollection>()
			{
				@Override
				public IOriCollection invoke() throws Throwable
				{
					return mergeIntern(cudResult, methodDescription);
				}
			});
		}
		finally
		{
			if (startTimeHasBeenSet)
			{
				startTimeTL.set(null);
			}
		}
	}

	protected IOriCollection mergeIntern(final ICUDResult cudResultOriginal, final IMethodDescription methodDescription)
	{
		IResultingBackgroundWorkerDelegate<IOriCollection> runnable = new IResultingBackgroundWorkerDelegate<IOriCollection>()
		{
			@Override
			public IOriCollection invoke() throws Throwable
			{
				IDisposableCache childCache = cacheFactory.createPrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, false, Boolean.FALSE,
						"MergeServiceRegistry.STATE");
				try
				{
					IncrementalMergeState state = (IncrementalMergeState) cudResultApplier.acquireNewState(childCache);
					ICUDResult cudResultOfCache;
					if (MergeProcess.isAddNewlyPersistedEntities())
					{
						cudResultOfCache = cudResultApplier.applyCUDResultOnEntitiesOfCache(cudResultOriginal, true, state);
					}
					else
					{
						cudResultOfCache = cudResultOriginal;
					}
					if (log.isDebugEnabled())
					{
						if (cudResultPrinter != null)
						{
							log.debug("Initial merge [" + System.identityHashCode(state) + "]:\n" + cudResultPrinter.printCUDResult(cudResultOfCache, state));
						}
						else
						{
							log.debug("Initial merge [" + System.identityHashCode(state) + "]. No Details available");
						}
					}
					IList<MergeOperation> mergeOperationSequence;
					ICUDResult extendedCudResult;
					if (cudResultOfCache != cudResultOriginal)
					{
						mergeOperationSequence = new ArrayList<MergeOperation>();
						extendedCudResult = whatIfMerged(cudResultOfCache, methodDescription, mergeOperationSequence, state);
					}
					else
					{
						extendedCudResult = cudResultOriginal;
						IMap<Class<?>, IList<IChangeContainer>> sortedChanges = bucketSortChanges(cudResultOriginal.getAllChanges());
						mergeOperationSequence = createMergeOperationSequence(sortedChanges);
					}
					if (log.isDebugEnabled())
					{
						log.debug("Merge finished [" + System.identityHashCode(state) + "]");
					}
					if (mergeSecurityManager != null)
					{
						mergeSecurityManager.checkMergeAccess(extendedCudResult, methodDescription);
					}
					ArrayList<Object> originalRefsOfCache = new ArrayList<Object>(cudResultOfCache.getOriginalRefs());
					ArrayList<Object> originalRefsExtended = new ArrayList<Object>(extendedCudResult.getOriginalRefs());
					IOriCollection oriCollExtended = intern(extendedCudResult, methodDescription, mergeOperationSequence, state);

					List<IChangeContainer> allChangesOriginal = cudResultOriginal.getAllChanges();
					List<IObjRef> allChangedObjRefsExtended = oriCollExtended.getAllChangeORIs();
					IObjRef[] allChangedObjRefsResult = new IObjRef[allChangesOriginal.size()];

					IdentityHashMap<Object, Integer> originalRefOfCacheToIndexMap = new IdentityHashMap<Object, Integer>();
					for (int a = originalRefsOfCache.size(); a-- > 0;)
					{
						originalRefOfCacheToIndexMap.put(originalRefsOfCache.get(a), Integer.valueOf(a));
					}
					for (int a = originalRefsExtended.size(); a-- > 0;)
					{
						Integer indexOfCache = originalRefOfCacheToIndexMap.get(originalRefsExtended.get(a));
						if (indexOfCache == null)
						{
							// this is a change implied by a rule or an persistence-implicit change
							// we do not know about it in the outer original CUDResult
							continue;
						}
						IObjRef objRefExtended = allChangedObjRefsExtended.get(a);
						IObjRef objRefOriginal = allChangesOriginal.get(indexOfCache.intValue()).getReference();
						if (objRefExtended == null)
						{
							// entity has been deleted
							objRefOriginal.setId(null);
							objRefOriginal.setVersion(null);
						}
						else
						{
							objRefOriginal.setId(objRefExtended.getId());
							objRefOriginal.setVersion(objRefExtended.getVersion());
						}
						if (objRefOriginal instanceof IDirectObjRef)
						{
							((IDirectObjRef) objRefOriginal).setDirect(null);
						}
						allChangedObjRefsResult[indexOfCache.intValue()] = objRefOriginal;
					}
					OriCollection oriCollection = new OriCollection(new ArrayList<IObjRef>(allChangedObjRefsResult));

					return oriCollection;
				}
				finally
				{
					childCache.dispose();
				}
			}
		};
		try
		{
			if (securityActive != null && securityActive.isFilterActivated())
			{
				return runnable.invoke();
			}
			else
			{
				return securityActive.executeWithoutFiltering(runnable);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected ICUDResult whatIfMerged(ICUDResult cudResult, final IMethodDescription methodDescription, IList<MergeOperation> mergeOperationSequence,
			final IncrementalMergeState incrementalState)
	{
		IList<MergeOperation> lastMergeOperationSequence;
		while (true)
		{
			IMap<Class<?>, IList<IChangeContainer>> sortedChanges = bucketSortChanges(cudResult.getAllChanges());
			lastMergeOperationSequence = createMergeOperationSequence(sortedChanges);

			final ParamHolder<Boolean> hasAtLeastOneImplicitChange = new ParamHolder<Boolean>(Boolean.FALSE);
			try
			{
				final IList<MergeOperation> fLastMergeOperationSequence = lastMergeOperationSequence;
				cudResult = cacheContext.executeWithCache(incrementalState.getStateCache(),
						new IResultingBackgroundWorkerParamDelegate<ICUDResult, ICUDResult>()
						{
							@Override
							public ICUDResult invoke(ICUDResult cudResult) throws Throwable
							{
								for (int a = 0, size = fLastMergeOperationSequence.size(); a < size; a++)
								{
									MergeOperation mergeOperation = fLastMergeOperationSequence.get(a);
									IMergeServiceExtension mergeServiceExtension = mergeOperation.getMergeServiceExtension();

									ICUDResult explAndImplCudResult = mergeServiceExtension.evaluateImplictChanges(cudResult, incrementalState);
									cudResult = mergeCudResult(cudResult, explAndImplCudResult, mergeServiceExtension, hasAtLeastOneImplicitChange,
											incrementalState);
								}
								return cudResult;
							}
						}, cudResult);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			for (IMergeListener mergeListener : mergeListeners.getExtensions())
			{
				ICUDResult explAndImplCudResult = mergeListener.preMerge(cudResult, incrementalState.getStateCache());
				cudResult = mergeCudResult(cudResult, explAndImplCudResult, mergeListener, hasAtLeastOneImplicitChange, incrementalState);
			}
			if (!Boolean.TRUE.equals(hasAtLeastOneImplicitChange.getValue()))
			{
				break;
			}
		}
		mergeOperationSequence.addAll(lastMergeOperationSequence);
		return cudResult;
	}

	protected IOriCollection intern(ICUDResult cudResult, IMethodDescription methodDescription, IList<MergeOperation> mergeOperationSequence,
			IncrementalMergeState state)
	{
		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		List<Object> originalRefs = cudResult.getOriginalRefs();
		IdentityHashMap<IChangeContainer, Integer> changeToChangeIndexDict = new IdentityHashMap<IChangeContainer, Integer>();

		for (int a = allChanges.size(); a-- > 0;)
		{
			changeToChangeIndexDict.put(allChanges.get(a), a);
		}
		IObjRef[] objRefs = new IObjRef[allChanges.size()];
		Long[] allChangedOn = new Long[allChanges.size()];
		String[] allChangedBy = new String[allChanges.size()];

		HashSet<Long> changedOnSet = new HashSet<Long>();
		HashSet<String> changedBySet = new HashSet<String>();

		for (int a = 0, size = mergeOperationSequence.size(); a < size; a++)
		{
			MergeOperation mergeOperation = mergeOperationSequence.get(a);
			IMergeServiceExtension mergeServiceExtension = mergeOperation.getMergeServiceExtension();

			IList<IChangeContainer> changesForMergeService = mergeOperation.getChangeContainer();
			ICUDResult msCudResult = buildCUDResult(changesForMergeService, changeToChangeIndexDict, originalRefs);

			IOriCollection msOriCollection = mergeServiceExtension.merge(msCudResult, methodDescription);

			mergeController.applyChangesToOriginals(msCudResult, msOriCollection, state.getStateCache());

			List<IObjRef> allChangeORIs = msOriCollection.getAllChangeORIs();

			Long msDefaultChangedOn = msOriCollection.getChangedOn();
			String msDefaultChangedBy = msOriCollection.getChangedBy();

			Long[] msAllChangedOn = msOriCollection.getAllChangedOn();
			String[] msAllChangedBy = msOriCollection.getAllChangedBy();
			for (int b = changesForMergeService.size(); b-- > 0;)
			{
				int index = changeToChangeIndexDict.get(changesForMergeService.get(b));
				objRefs[index] = allChangeORIs.get(b);

				if (msAllChangedOn != null)
				{
					Long msChangedOn = msAllChangedOn[b];
					allChangedOn[index] = msChangedOn;
					changedOnSet.add(msChangedOn);
				}
				else
				{
					allChangedOn[index] = msDefaultChangedOn;
				}
				if (msAllChangedBy != null)
				{
					String msChangedBy = msAllChangedBy[b];
					allChangedBy[index] = msChangedBy;
					changedBySet.add(msChangedBy);
				}
				else
				{
					allChangedBy[index] = msDefaultChangedBy;
				}
			}
			if (msDefaultChangedOn != null)
			{
				changedOnSet.add(msDefaultChangedOn);
			}
			if (msDefaultChangedBy != null)
			{
				changedBySet.add(msDefaultChangedBy);
			}
		}
		OriCollection oriCollection = new OriCollection();
		oriCollection.setAllChangeORIs(new ArrayList<IObjRef>(objRefs));

		if (changedBySet.size() == 1)
		{
			oriCollection.setChangedBy(changedBySet.iterator().next());
		}
		else
		{
			oriCollection.setAllChangedBy(allChangedBy);
		}
		if (changedOnSet.size() == 1)
		{
			oriCollection.setChangedOn(changedOnSet.iterator().next());
		}
		else
		{
			oriCollection.setAllChangedOn(allChangedOn);
		}
		for (IMergeListener mergeListener : mergeListeners.getExtensions())
		{
			mergeListener.postMerge(cudResult, objRefs);
		}
		if (originalRefs != null)
		{
			// Set each original ref to null in order to suppress a post-processing in a potentially calling IMergeProcess
			for (int a = originalRefs.size(); a-- > 0;)
			{
				originalRefs.set(a, null);
			}
		}
		// TODO DCE must be fired HERE <---
		return oriCollection;
	}

	protected ICUDResult mergeCudResult(ICUDResult cudResult, ICUDResult explAndImplCudResult, Object implyingHandle,
			ParamHolder<Boolean> hasAtLeastOneImplicitChange, IncrementalMergeState state)
	{
		if (explAndImplCudResult == null || cudResult == explAndImplCudResult)
		{
			return cudResult;
		}
		ICUDResult diffCUDResult = cudResultComparer.diffCUDResult(cudResult, explAndImplCudResult);
		if (diffCUDResult == null)
		{
			return cudResult;
		}
		hasAtLeastOneImplicitChange.setValue(Boolean.TRUE);
		cudResultApplier.applyCUDResultOnEntitiesOfCache(diffCUDResult, false, state);
		if (log.isDebugEnabled())
		{
			Object currHandle = implyingHandle;
			if (currHandle instanceof Factory)
			{
				MethodInterceptor interceptor = (MethodInterceptor) ((Factory) currHandle).getCallbacks()[0];
				while (interceptor instanceof CascadedInterceptor)
				{
					Object target = ((CascadedInterceptor) interceptor).getTarget();
					if (target instanceof MethodInterceptor)
					{
						interceptor = ((MethodInterceptor) target);
						continue;
					}
					currHandle = target;
					break;
				}
			}
			if (currHandle == null)
			{
				currHandle = implyingHandle;
			}
			if (cudResultPrinter != null)
			{
				log.debug("Incremental merge [" + System.identityHashCode(state) + "] (" + currHandle.getClass().getSimpleName() + "):\n"
						+ cudResultPrinter.printCUDResult(diffCUDResult, state));
			}
			else
			{
				log.debug("Incremental merge [" + System.identityHashCode(state) + "]  (" + currHandle.getClass().getSimpleName() + "). No Details printable");
			}
		}
		return explAndImplCudResult;
	}

	protected ICUDResult buildCUDResult(IList<IChangeContainer> changesForMergeService, IMap<IChangeContainer, Integer> changeToChangeIndexDict,
			List<Object> originalRefs)
	{
		Object[] msOriginalRefs = new Object[changesForMergeService.size()];
		for (int b = changesForMergeService.size(); b-- > 0;)
		{
			int index = changeToChangeIndexDict.get(changesForMergeService.get(b)).intValue();
			if (originalRefs != null)
			{
				msOriginalRefs[b] = originalRefs.get(index);
			}
		}
		return new CUDResult(changesForMergeService, new ArrayList<Object>(msOriginalRefs));
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		IdentityHashMap<IMergeServiceExtension, List<Class<?>>> mseToEntityTypes = new IdentityHashMap<IMergeServiceExtension, List<Class<?>>>();

		for (int a = entityTypes.size(); a-- > 0;)
		{
			Class<?> entityType = entityTypes.get(a);
			IMergeServiceExtension mergeServiceExtension = mergeServiceExtensions.getExtension(entityType);
			if (mergeServiceExtension == null)
			{
				continue;
			}
			List<Class<?>> groupedEntityTypes = mseToEntityTypes.get(mergeServiceExtension);
			if (groupedEntityTypes == null)
			{
				groupedEntityTypes = new ArrayList<Class<?>>();
				mseToEntityTypes.put(mergeServiceExtension, groupedEntityTypes);
			}
			groupedEntityTypes.add(entityType);
		}
		ArrayList<IEntityMetaData> metaDataResult = new ArrayList<IEntityMetaData>(entityTypes.size());
		for (Entry<IMergeServiceExtension, List<Class<?>>> entry : mseToEntityTypes)
		{
			List<IEntityMetaData> groupedMetaData = entry.getKey().getMetaData(entry.getValue());
			metaDataResult.addAll(groupedMetaData);
		}
		return metaDataResult;
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return entityMetaDataProvider.getValueObjectConfig(valueType);
	}

	protected IMergeServiceExtension getServiceForType(Class<?> type)
	{
		return getServiceForType(type, false);
	}

	protected IMergeServiceExtension getServiceForType(Class<?> type, boolean tryOnly)
	{
		if (type == null)
		{
			return null;
		}
		IMergeServiceExtension mse = mergeServiceExtensions.getExtension(type);
		if (mse == null && !tryOnly)
		{
			throw new IllegalArgumentException("No " + IMergeServiceExtension.class.getSimpleName() + " found to handle entity type '" + type.getName() + "'");
		}
		return mse;
	}

	protected IMap<Class<?>, IList<IChangeContainer>> bucketSortChanges(List<IChangeContainer> allChanges)
	{
		IMap<Class<?>, IList<IChangeContainer>> sortedChanges = new HashMap<Class<?>, IList<IChangeContainer>>();

		for (int i = allChanges.size(); i-- > 0;)
		{
			IChangeContainer changeContainer = allChanges.get(i);
			IObjRef objRef = changeContainer.getReference();
			Class<?> type = objRef.getRealType();
			IList<IChangeContainer> changeContainers = sortedChanges.get(type);
			if (changeContainers == null)
			{
				changeContainers = new ArrayList<IChangeContainer>();
				if (!sortedChanges.putIfNotExists(type, changeContainers))
				{
					throw new IllegalStateException("Key already exists " + type);
				}
			}
			changeContainers.add(changeContainer);
		}
		return sortedChanges;
	}

	protected IList<MergeOperation> createMergeOperationSequence(IMap<Class<?>, IList<IChangeContainer>> sortedChanges)
	{
		Class<?>[] entityPersistOrder = entityMetaDataProvider.getEntityPersistOrder();
		final IList<MergeOperation> mergeOperations = new ArrayList<MergeOperation>();

		if (entityPersistOrder != null)
		{
			for (int a = entityPersistOrder.length; a-- > 0;)
			{
				Class<?> orderedEntityType = entityPersistOrder[a];
				IList<IChangeContainer> changes = sortedChanges.get(orderedEntityType);
				if (changes == null)
				{
					// No changes of current type found. Nothing to do here
					continue;
				}
				IList<IChangeContainer> removes = new ArrayList<IChangeContainer>(changes.size());
				IList<IChangeContainer> insertsAndUpdates = new ArrayList<IChangeContainer>(changes.size());
				for (int b = changes.size(); b-- > 0;)
				{
					IChangeContainer change = changes.get(b);
					if (change instanceof DeleteContainer)
					{
						removes.add(change);
					}
					else
					{
						insertsAndUpdates.add(change);
					}
				}
				if (removes.size() == 0)
				{
					// Nothing to do. Ordering is not necessary here
					continue;
				}
				if (insertsAndUpdates.size() == 0)
				{
					sortedChanges.remove(orderedEntityType);
				}
				else
				{
					sortedChanges.put(orderedEntityType, insertsAndUpdates);
				}
				IMergeServiceExtension mergeServiceExtension = getServiceForType(orderedEntityType);
				MergeOperation mergeOperation = new MergeOperation();
				mergeOperation.setMergeServiceExtension(mergeServiceExtension);
				mergeOperation.setChangeContainer(removes);

				mergeOperations.add(mergeOperation);
			}
			for (int a = 0, size = entityPersistOrder.length; a < size; a++)
			{
				Class<?> orderedEntityType = entityPersistOrder[a];
				IList<IChangeContainer> changes = sortedChanges.get(orderedEntityType);
				if (changes == null)
				{
					// No changes of current type found. Nothing to do here
					continue;
				}
				boolean containsNew = false;
				for (int b = changes.size(); b-- > 0;)
				{
					if (changes.get(b).getReference().getId() == null)
					{
						containsNew = true;
						break;
					}
				}
				if (!containsNew)
				{
					// Nothing to do. Ordering is not necessary here
					continue;
				}
				// Remove batch of changes where at least 1 new entity occured
				// and
				// this type of entity has to be inserted in a global order
				sortedChanges.remove(orderedEntityType);
				IMergeServiceExtension mergeServiceExtension = getServiceForType(orderedEntityType);
				MergeOperation mergeOperation = new MergeOperation();
				mergeOperation.setMergeServiceExtension(mergeServiceExtension);
				mergeOperation.setChangeContainer(changes);

				mergeOperations.add(mergeOperation);
			}
		}

		// Everything which is left in the sortedChanges map can be merged
		// without global order, so batch together as much as possible

		for (Entry<Class<?>, IList<IChangeContainer>> entry : sortedChanges)
		{
			Class<?> type = entry.getKey();
			IList<IChangeContainer> unorderedChanges = entry.getValue();
			IMergeServiceExtension mergeServiceExtension = getServiceForType(type, true);

			if (mergeServiceExtension == null)
			{
				throw new IllegalStateException("No extension found to merge entities of type '" + type.getName() + "'");
			}
			boolean cont = false;
			for (MergeOperation existingMergeOperation : mergeOperations)
			{
				if (existingMergeOperation.getMergeServiceExtension() == mergeServiceExtension)
				{
					IList<IChangeContainer> orderedChanges = existingMergeOperation.getChangeContainer();
					for (int b = unorderedChanges.size(); b-- > 0;)
					{
						orderedChanges.add(unorderedChanges.get(b));
					}
					cont = true;
					break;
				}
			}
			if (cont)
			{
				continue;
			}
			MergeOperation mergeOperation = new MergeOperation();
			mergeOperation.setMergeServiceExtension(mergeServiceExtension);
			mergeOperation.setChangeContainer(unorderedChanges);

			mergeOperations.add(mergeOperation);
		}
		return mergeOperations;
	}

	@Override
	public long getStartTime()
	{
		Long startTime = startTimeTL.get();
		if (startTime == null)
		{
			throw new IllegalStateException("No merge process is currently active");
		}
		return startTime.longValue();
	}

	@Override
	public void registerMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Class<?> entityType)
	{
		mergeServiceExtensions.register(mergeServiceExtension, entityType);
	}

	@Override
	public void unregisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Class<?> entityType)
	{
		mergeServiceExtensions.unregister(mergeServiceExtension, entityType);
	}

	@Override
	public void registerMergeListener(IMergeListener mergeListener)
	{
		mergeListeners.register(mergeListener);
	}

	@Override
	public void unregisterMergeListener(IMergeListener mergeListener)
	{
		mergeListeners.unregister(mergeListener);
	}
}