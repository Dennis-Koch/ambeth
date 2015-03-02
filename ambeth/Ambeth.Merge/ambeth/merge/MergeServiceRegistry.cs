using System;
using System.Collections;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Merge.Incremental;
using System.Runtime.CompilerServices;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Ioc.Threadlocal;

namespace De.Osthus.Ambeth.Cache
{
    public class MergeServiceRegistry : IMergeService, IMergeServiceExtensionExtendable, IMergeListenerExtendable, IThreadLocalCleanupBean
    {
        public class MergeOperation
        {
            public IMergeServiceExtension MergeServiceExtension { get; set; }

            public IList<IChangeContainer> ChangeContainer { get; set; }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ICacheContext CacheContext { protected get; set; }

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        [Autowired]
        public ICUDResultApplier CudResultApplier { protected get; set; }

        [Autowired]
        public ICUDResultComparer CudResultComparer { protected get; set; }

        [Autowired(Optional = true)]
        public ICUDResultPrinter CudResultPrinter { protected get; set; }
        
        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IEventDispatcher EventDispatcher { protected get; set; }

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IMergeController MergeController { protected get; set; }

        [Autowired(Optional = true)]
        public IMergeSecurityManager MergeSecurityManager { protected get; set; }

        [Autowired]
        public ISecurityActivation SecurityActive { protected get; set; }

        [Autowired(Optional = true)]
        public ILightweightTransaction Transaction { protected get; set; }

        protected readonly ClassExtendableContainer<IMergeServiceExtension> mergeServiceExtensions = new ClassExtendableContainer<IMergeServiceExtension>("mergeServiceExtension", "entityType");

        protected readonly DefaultExtendableContainer<IMergeListener> mergeListeners = new DefaultExtendableContainer<IMergeListener>("mergeListener");
        
        [Forkable]
	    protected readonly ThreadLocal<long?> startTimeTL = new ThreadLocal<long?>();

	    public void CleanupThreadLocal()
	    {
            // intended blank. Interface is just needed to make the @Forkable annotation work
	    }

        public IOriCollection Merge(ICUDResult cudResult, IMethodDescription methodDescription)
        {
            ParamChecker.AssertParamNotNull(cudResult, "cudResult");

            long? startTime = startTimeTL.Value;
            bool startTimeHasBeenSet = false;
            if (!startTime.HasValue)
            {
                startTime = DateTimeUtil.CurrentTimeMillis();
                startTimeTL.Value = startTime;
                startTimeHasBeenSet = true;
            }
            try
            {
                if (Transaction == null || Transaction.Active)
                {
                    return MergeIntern(cudResult, methodDescription);
                }
                return Transaction.RunInLazyTransaction(delegate()
                    {
                        return MergeIntern(cudResult, methodDescription);
                    });
            }
            finally
            {
                if (startTimeHasBeenSet)
                {
                    startTimeTL.Value = null;
                }
            }
        }

	    protected IOriCollection MergeIntern(ICUDResult cudResultOriginal, IMethodDescription methodDescription)
	    {
		    IResultingBackgroundWorkerDelegate<IOriCollection> runnable = new IResultingBackgroundWorkerDelegate<IOriCollection>(delegate()
		    {
				IDisposableCache childCache = CacheFactory.CreatePrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, false, false,
						"MergeServiceRegistry.STATE");
				try
				{
					IncrementalMergeState state = (IncrementalMergeState) CudResultApplier.AcquireNewState(childCache);
					ICUDResult cudResultOfCache = CudResultApplier.ApplyCUDResultOnEntitiesOfCache(cudResultOriginal, true, state);
					if (Log.DebugEnabled)
					{
						if (CudResultPrinter != null)
						{
							Log.Debug("Initial merge [" + RuntimeHelpers.GetHashCode(state) + "]:\n" + CudResultPrinter.PrintCUDResult(cudResultOfCache, state));
						}
						else
						{
                            Log.Debug("Initial merge [" + RuntimeHelpers.GetHashCode(state) + "]. No Details available");
						}
					}
					List<MergeOperation> mergeOperationSequence = new List<MergeOperation>();
					ICUDResult extendedCudResult = WhatIfMerged(cudResultOfCache, methodDescription, mergeOperationSequence, state);

					if (Log.DebugEnabled)
					{
                        Log.Debug("Merge finished [" + RuntimeHelpers.GetHashCode(state) + "]");
					}
					if (MergeSecurityManager != null)
					{
						MergeSecurityManager.CheckMergeAccess(extendedCudResult, methodDescription);
					}
					List<Object> originalRefsOfCache = new List<Object>(cudResultOfCache.GetOriginalRefs());
					List<Object> originalRefsExtended = new List<Object>(extendedCudResult.GetOriginalRefs());
					IOriCollection oriCollExtended = Intern(extendedCudResult, methodDescription, mergeOperationSequence, state);

					IList<IChangeContainer> allChangesOriginal = cudResultOriginal.AllChanges;
					IList<IObjRef> allChangedObjRefsExtended = oriCollExtended.AllChangeORIs;
                    IObjRef[] allChangedObjRefsResult = new IObjRef[allChangesOriginal.Count];

                    IdentityHashMap<Object, int?> originalRefOfCacheToIndexMap = new IdentityHashMap<Object, int?>();
					for (int a = originalRefsOfCache.Count; a-- > 0;)
					{
						originalRefOfCacheToIndexMap.Put(originalRefsOfCache[a], a);
					}
					for (int a = originalRefsExtended.Count; a-- > 0;)
					{
						int? indexOfCache = originalRefOfCacheToIndexMap.Get(originalRefsExtended[a]);
						if (indexOfCache == null)
						{
							// this is a change implied by a rule or an persistence-implicit change
							// we do not know about it in the outer original CUDResult
							continue;
						}
						IObjRef objRefExtended = allChangedObjRefsExtended[a];
						IObjRef objRefOriginal = allChangesOriginal[indexOfCache.Value].Reference;
						if (objRefExtended == null)
						{
							// entity has been deleted
							objRefOriginal.Id = null;
							objRefOriginal.Version = null;
						}
						else
						{
							objRefOriginal.Id = objRefExtended.Id;
							objRefOriginal.Version = objRefExtended.Version;
						}
						if (objRefOriginal is IDirectObjRef)
						{
							((IDirectObjRef) objRefOriginal).Direct = null;
						}
                        allChangedObjRefsResult[indexOfCache.Value] = objRefOriginal;
					}
					OriCollection oriCollection = new OriCollection(new List<IObjRef>(allChangedObjRefsResult));

					return oriCollection;
				}
				finally
				{
					childCache.Dispose();
				}
		    });
			if (SecurityActive != null && SecurityActive.FilterActivated)
			{
				return runnable();
			}
			else
			{
				return SecurityActive.ExecuteWithoutFiltering(runnable);
			}
	    }

	    protected ICUDResult WhatIfMerged(ICUDResult cudResult, IMethodDescription methodDescription, List<MergeOperation> mergeOperationSequence,
			    IncrementalMergeState incrementalState)
	    {
		    IList<MergeOperation> lastMergeOperationSequence;
		    while (true)
		    {
			    IMap<Type, IList<IChangeContainer>> sortedChanges = BucketSortChanges(cudResult.AllChanges);
			    lastMergeOperationSequence = CreateMergeOperationSequence(sortedChanges);

			    ParamHolder<bool> hasAtLeastOneImplicitChange = new ParamHolder<bool>(false);
				IList<MergeOperation> fLastMergeOperationSequence = lastMergeOperationSequence;
				cudResult = CacheContext.ExecuteWithCache(incrementalState.GetStateCache(), delegate(ICUDResult cudResult2)
						{
							for (int a = 0, size = fLastMergeOperationSequence.Count; a < size; a++)
							{
								MergeOperation mergeOperation = fLastMergeOperationSequence[a];
								IMergeServiceExtension mergeServiceExtension = mergeOperation.MergeServiceExtension;

								ICUDResult explAndImplCudResult = mergeServiceExtension.EvaluateImplicitChanges(cudResult2, incrementalState);
								cudResult2 = MergeCudResult(cudResult2, explAndImplCudResult, mergeServiceExtension, hasAtLeastOneImplicitChange,
										incrementalState);
							}
							return cudResult2;
						}, cudResult);
			    foreach (IMergeListener mergeListener in mergeListeners.GetExtensions())
			    {
				    ICUDResult explAndImplCudResult = mergeListener.PreMerge(cudResult, incrementalState.GetStateCache());
				    cudResult = MergeCudResult(cudResult, explAndImplCudResult, mergeListener, hasAtLeastOneImplicitChange, incrementalState);
			    }
			    if (!hasAtLeastOneImplicitChange.Value)
			    {
				    break;
			    }
		    }
		    mergeOperationSequence.AddRange(lastMergeOperationSequence);
		    return cudResult;
	    }

	    protected IOriCollection Intern(ICUDResult cudResult, IMethodDescription methodDescription, IList<MergeOperation> mergeOperationSequence,
			    IncrementalMergeState state)
	    {
		    IList<IChangeContainer> allChanges = cudResult.AllChanges;
		    IList<Object> originalRefs = cudResult.GetOriginalRefs();
		    IdentityHashMap<IChangeContainer, int> changeToChangeIndexDict = new IdentityHashMap<IChangeContainer, int>();

		    for (int a = allChanges.Count; a-- > 0;)
		    {
			    changeToChangeIndexDict.Put(allChanges[a], a);
		    }
		    IObjRef[] objRefs = new IObjRef[allChanges.Count];
            long[] allChangedOn = new long[allChanges.Count];
		    String[] allChangedBy = new String[allChanges.Count];

		    CHashSet<long> changedOnSet = new CHashSet<long>();
		    CHashSet<String> changedBySet = new CHashSet<String>();

		    for (int a = 0, size = mergeOperationSequence.Count; a < size; a++)
		    {
			    MergeOperation mergeOperation = mergeOperationSequence[a];
			    IMergeServiceExtension mergeServiceExtension = mergeOperation.MergeServiceExtension;

			    IList<IChangeContainer> changesForMergeService = mergeOperation.ChangeContainer;
			    ICUDResult msCudResult = BuildCUDResult(changesForMergeService, changeToChangeIndexDict, originalRefs);

			    IOriCollection msOriCollection = mergeServiceExtension.Merge(msCudResult, methodDescription);

			    MergeController.ApplyChangesToOriginals(msCudResult, msOriCollection, state.GetStateCache());

			    IList<IObjRef> allChangeORIs = msOriCollection.AllChangeORIs;

			    long? msDefaultChangedOn = msOriCollection.ChangedOn;
			    String msDefaultChangedBy = msOriCollection.ChangedBy;

			    long[] msAllChangedOn = msOriCollection.AllChangedOn;
			    String[] msAllChangedBy = msOriCollection.AllChangedBy;
			    for (int b = changesForMergeService.Count; b-- > 0;)
			    {
				    int index = changeToChangeIndexDict.Get(changesForMergeService[b]);
				    objRefs[index] = allChangeORIs[b];

				    if (msAllChangedOn != null)
				    {
					    long msChangedOn = msAllChangedOn[b];
					    allChangedOn[index] = msChangedOn;
					    changedOnSet.Add(msChangedOn);
				    }
				    else
				    {
					    allChangedOn[index] = msDefaultChangedOn.Value;
				    }
				    if (msAllChangedBy != null)
				    {
					    String msChangedBy = msAllChangedBy[b];
					    allChangedBy[index] = msChangedBy;
					    changedBySet.Add(msChangedBy);
				    }
				    else
				    {
					    allChangedBy[index] = msDefaultChangedBy;
				    }
			    }
			    if (msDefaultChangedOn != null)
			    {
				    changedOnSet.Add(msDefaultChangedOn.Value);
			    }
			    if (msDefaultChangedBy != null)
			    {
				    changedBySet.Add(msDefaultChangedBy);
			    }
		    }
		    OriCollection oriCollection = new OriCollection();
		    oriCollection.AllChangeORIs = new List<IObjRef>(objRefs);

		    if (changedBySet.Count == 1)
		    {
                Iterator<String> iter = changedBySet.Iterator();
                iter.MoveNext();
			    oriCollection.ChangedBy = iter.Current;
		    }
		    else
		    {
			    oriCollection.AllChangedBy = allChangedBy;
		    }
		    if (changedOnSet.Count == 1)
		    {
                Iterator<long> iter = changedOnSet.Iterator();
                iter.MoveNext();
                oriCollection.ChangedOn = iter.Current;
		    }
		    else
		    {
			    oriCollection.AllChangedOn = allChangedOn;
		    }
		    foreach (IMergeListener mergeListener in mergeListeners.GetExtensions())
		    {
			    mergeListener.PostMerge(cudResult, objRefs);
		    }
		    if (originalRefs != null)
		    {
			    // Set each original ref to null in order to suppress a post-processing in a potentially calling IMergeProcess
			    for (int a = originalRefs.Count; a-- > 0;)
			    {
				    originalRefs[a] = null;
			    }
		    }
		    // TODO DCE must be fired HERE <---
		    return oriCollection;
	    }

	    protected ICUDResult MergeCudResult(ICUDResult cudResult, ICUDResult explAndImplCudResult, Object implyingHandle,
			    ParamHolder<Boolean> hasAtLeastOneImplicitChange, IncrementalMergeState state)
	    {
		    if (explAndImplCudResult == null || Object.ReferenceEquals(cudResult, explAndImplCudResult))
		    {
			    return cudResult;
		    }
		    ICUDResult diffCUDResult = CudResultComparer.DiffCUDResult(cudResult, explAndImplCudResult);
		    if (diffCUDResult == null)
		    {
			    return cudResult;
		    }
		    hasAtLeastOneImplicitChange.Value = true;
		    CudResultApplier.ApplyCUDResultOnEntitiesOfCache(diffCUDResult, false, state);
		    if (Log.DebugEnabled)
		    {
			    Object currHandle = implyingHandle;
                if (currHandle is IProxyTargetAccessor)
			    {
                    IInterceptor interceptor = ((IProxyTargetAccessor)currHandle).GetInterceptors()[0];
				    while (interceptor is CascadedInterceptor)
				    {
					    Object target = ((CascadedInterceptor) interceptor).Target;
                        if (target is IInterceptor)
					    {
                            interceptor = ((IInterceptor)target);
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
			    if (CudResultPrinter != null)
			    {
                    Log.Debug("Incremental merge [" + RuntimeHelpers.GetHashCode(state) + "] (" + currHandle.GetType().Name + "):\n"
                            + CudResultPrinter.PrintCUDResult(diffCUDResult, state));
			    }
			    else
			    {
                    Log.Debug("Incremental merge [" + RuntimeHelpers.GetHashCode(state) + "]  (" + currHandle.GetType().Name + "). No Details printable");
			    }
		    }
		    return explAndImplCudResult;
	    }

	    protected ICUDResult BuildCUDResult(IList<IChangeContainer> changesForMergeService, IMap<IChangeContainer, int> changeToChangeIndexDict,
			    IList<Object> originalRefs)
	    {
		    Object[] msOriginalRefs = new Object[changesForMergeService.Count];
		    for (int b = changesForMergeService.Count; b-- > 0;)
		    {
			    int index = changeToChangeIndexDict.Get(changesForMergeService[b]);
			    if (originalRefs != null)
			    {
				    msOriginalRefs[b] = originalRefs[index];
			    }
		    }
		    return new CUDResult(changesForMergeService, new List<Object>(msOriginalRefs));
	    }

        public IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            IdentityHashMap<IMergeServiceExtension, IList<Type>> mseToEntityTypes = new IdentityHashMap<IMergeServiceExtension, IList<Type>>();

		    for (int a = entityTypes.Count; a-- > 0;)
		    {
			    Type entityType = entityTypes[a];
			    IMergeServiceExtension mergeServiceExtension = mergeServiceExtensions.GetExtension(entityType);
                if (mergeServiceExtension == null)
                {
                    continue;
                }
                IList<Type> groupedEntityTypes = mseToEntityTypes.Get(mergeServiceExtension);
			    if (groupedEntityTypes == null)
			    {
				    groupedEntityTypes = new List<Type>();
				    mseToEntityTypes.Put(mergeServiceExtension, groupedEntityTypes);
			    }
			    groupedEntityTypes.Add(entityType);
		    }
		    List<IEntityMetaData> metaDataResult = new List<IEntityMetaData>(entityTypes.Count);
		    foreach (Entry<IMergeServiceExtension, IList<Type>> entry in mseToEntityTypes)
		    {
			    IList<IEntityMetaData> groupedMetaData = entry.Key.GetMetaData(entry.Value);
			    metaDataResult.AddRange(groupedMetaData);
		    }
		    return metaDataResult;
        }

        public virtual IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            return EntityMetaDataProvider.GetValueObjectConfig(valueType);
        }

        protected IMergeServiceExtension GetServiceForType(Type type)
        {
            if (type == null)
            {
                return null;
            }
            IMergeServiceExtension mse = mergeServiceExtensions.GetExtension(type);
            if (mse == null)
            {
                throw new Exception("No merge service found to handle entity type '" + type.FullName + "'");
            }
            return mse;
        }

        protected IMap<Type, IList<IChangeContainer>> BucketSortChanges(IList<IChangeContainer> allChanges)
        {
            HashMap<Type, IList<IChangeContainer>> sortedChanges = new HashMap<Type, IList<IChangeContainer>>();

            for (int i = allChanges.Count; i-- > 0; )
            {
                IChangeContainer changeContainer = allChanges[i];
                IObjRef objRef = changeContainer.Reference;
                Type type = objRef.RealType;
                IList<IChangeContainer> changeContainers = sortedChanges.Get(type);
                if (changeContainers == null)
                {
                    changeContainers = new List<IChangeContainer>();
                    sortedChanges.Put(type, changeContainers);
                }
                changeContainers.Add(changeContainer);
            }
            return sortedChanges;
        }

        protected IList<MergeOperation> CreateMergeOperationSequence(IMap<Type, IList<IChangeContainer>> sortedChanges)
        {
            Type[] entityPersistOrder = EntityMetaDataProvider.GetEntityPersistOrder();
            IList<MergeOperation> mergeOperations = new List<MergeOperation>();
            
            if (entityPersistOrder != null)
            {
                for (int a = entityPersistOrder.Length; a-- > 0; )
                {
                    Type orderedEntityType = entityPersistOrder[a];
                    IList<IChangeContainer> changes = sortedChanges.Get(orderedEntityType);
                    if (changes == null)
                    {
                        // No changes of current type found. Nothing to do here
                        continue;
                    }
                    List<IChangeContainer> removes = new List<IChangeContainer>(changes.Count);
                    List<IChangeContainer> insertsAndUpdates = new List<IChangeContainer>(changes.Count);
                    for (int b = changes.Count; b-- > 0; )
                    {
                        IChangeContainer change = changes[b];
                        if (change is DeleteContainer)
                        {
                            removes.Add(change);
                        }
                        else
                        {
                            insertsAndUpdates.Add(change);
                        }
                    }
                    if (removes.Count == 0)
                    {
                        // Nothing to do. Ordering is not necessary here
                        continue;
                    }
                    if (insertsAndUpdates.Count == 0)
                    {
                        sortedChanges.Remove(orderedEntityType);
                    }
                    else
                    {
                        sortedChanges.Put(orderedEntityType, insertsAndUpdates);
                    }
                    IMergeServiceExtension mergeServiceExtension = GetServiceForType(orderedEntityType);
                    MergeOperation mergeOperation = new MergeOperation();
                    mergeOperation.MergeServiceExtension = mergeServiceExtension;
                    mergeOperation.ChangeContainer = removes;

                    mergeOperations.Add(mergeOperation);
                }

                for (int a = 0, size = entityPersistOrder.Length; a < size; a++)
                {
                    Type orderedEntityType = entityPersistOrder[a];
                    IList<IChangeContainer> changes = sortedChanges.Get(orderedEntityType);
                    if (changes == null)
                    {
                        // No changes of current type found. Nothing to do here
                        continue;
                    }
                    bool containsNew = false;
                    for (int b = changes.Count; b-- > 0; )
                    {
                        if (changes[b].Reference.Id == null)
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
                    // Remove batch of changes where at least 1 new entity occured and
                    // this type of entity has to be inserted in a global order
                    sortedChanges.Remove(orderedEntityType);
                    IMergeServiceExtension mergeServiceExtension = GetServiceForType(orderedEntityType);
                    MergeOperation mergeOperation = new MergeOperation();
                    mergeOperation.MergeServiceExtension = mergeServiceExtension;
                    mergeOperation.ChangeContainer = changes;

                    mergeOperations.Add(mergeOperation);
                }
            }

            // Everything which is left in the sortedChanges map can be merged without global order, so batch together as much as possible
            foreach (Entry<Type, IList<IChangeContainer>> entry in sortedChanges)
		    {
			    Type type = entry.Key;
			    IList<IChangeContainer> unorderedChanges = entry.Value;
                IMergeServiceExtension mergeServiceExtension = GetServiceForType(type);

                if (mergeServiceExtension == null)
                {
                    throw new Exception("No extension found to merge entities of type '" + type.FullName + "'");
                }
                bool cont = false;
                foreach (MergeOperation existingMergeOperation in mergeOperations)
                {
                    if (Object.ReferenceEquals(existingMergeOperation.MergeServiceExtension, mergeServiceExtension))
                    {
                        IList<IChangeContainer> orderedChanges = existingMergeOperation.ChangeContainer;
                        for (int b = unorderedChanges.Count; b-- > 0; )
                        {
                            orderedChanges.Add(unorderedChanges[b]);
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
                mergeOperation.MergeServiceExtension = mergeServiceExtension;
                mergeOperation.ChangeContainer = unorderedChanges;

                mergeOperations.Add(mergeOperation);
            };
            return mergeOperations;
        }

        public long GetStartTime()
        {
            long? startTime = startTimeTL.Value;
            if (!startTime.HasValue)
            {
                throw new Exception("No merge process is currently active");
            }
            return startTime.Value;
        }

	    public void RegisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Type entityType)
	    {
		    mergeServiceExtensions.Register(mergeServiceExtension, entityType);
	    }

	    public void UnregisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Type entityType)
	    {
		    mergeServiceExtensions.Unregister(mergeServiceExtension, entityType);
	    }

	    public void RegisterMergeListener(IMergeListener mergeListener)
	    {
		    mergeListeners.Register(mergeListener);
	    }

	    public void UnregisterMergeListener(IMergeListener mergeListener)
	    {
		    mergeListeners.Unregister(mergeListener);
	    }
    }
}
