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

namespace De.Osthus.Ambeth.Cache
{
    public class MergeServiceRegistry : IMergeService, IMergeServiceExtensionExtendable
    {
        public class MergeOperation
        {
            public IMergeServiceExtension MergeServiceExtension { get; set; }

            public IList<IChangeContainer> ChangeContainer { get; set; }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IEventDispatcher EventDispatcher { protected get; set; }

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IMergeController MergeController { protected get; set; }

        protected readonly ClassExtendableContainer<IMergeServiceExtension> mergeServiceExtensions = new ClassExtendableContainer<IMergeServiceExtension>("mergeServiceExtension", "entityType");

        public void RegisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Type entityType)
        {
            mergeServiceExtensions.Register(mergeServiceExtension, entityType);
        }

        public void UnregisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Type entityType)
        {
            mergeServiceExtensions.Unregister(mergeServiceExtension, entityType);
        }

        public IOriCollection Merge(ICUDResult cudResult, IMethodDescription methodDescription)
        {
            ParamChecker.AssertParamNotNull(cudResult, "cudResult");

            IList<IChangeContainer> allChanges = cudResult.AllChanges;
            IList<Object> originalRefs = cudResult.GetOriginalRefs();

            IDictionary<IChangeContainer, int> changeToChangeIndexDict = new IdentityDictionary<IChangeContainer, int>();
            for (int a = allChanges.Count; a-- > 0; )
            {
                changeToChangeIndexDict.Add(allChanges[a], a);
            }

            IDictionary<Type, IList<IChangeContainer>> sortedChanges = BucketSortChanges(allChanges);
            IList<MergeOperation> mergeOperationSequence = CreateMergeOperationSequence(sortedChanges);

            OriCollection oriCollection = new OriCollection();

            IList<IOriCollection> hardRefsToNestedOriCollections = new List<IOriCollection>();
            IObjRef[] objRefs = new IObjRef[allChanges.Count];

            EventDispatcher.EnableEventQueue();
            try
            {
                for (int a = 0, size = mergeOperationSequence.Count; a < size; a++)
                {
                    MergeOperation mergeOperation = mergeOperationSequence[a];
                    IMergeServiceExtension mergeServiceExtension = mergeOperation.MergeServiceExtension;
                    IList<IChangeContainer> changesForMergeService = mergeOperation.ChangeContainer;

                    Object[] msOriginalRefs = new Object[changesForMergeService.Count];
                    for (int b = changesForMergeService.Count; b-- > 0; )
                    {
                        int index = changeToChangeIndexDict[changesForMergeService[b]];
                        msOriginalRefs[b] = originalRefs[index];
                    }
                    CUDResult msCudResult = new CUDResult(changesForMergeService, msOriginalRefs);
                    IOriCollection msOriCollection = mergeServiceExtension.Merge(msCudResult, methodDescription);

                    // Store the result of the merge operation as a hard ref. as long as we maintain the ref, the rootcache will
                    // NOT loose related information due to any GC
                    hardRefsToNestedOriCollections.Add(msOriCollection);

                    PostProcessOriCollection(msCudResult, msOriCollection);

                    IList<IObjRef> allChangeORIs = msOriCollection.AllChangeORIs;
                    for (int b = changesForMergeService.Count; b-- > 0; )
                    {
                        int index = changeToChangeIndexDict[changesForMergeService[b]];
                        objRefs[index] = allChangeORIs[b];
                    }
                }
                oriCollection.AllChangeORIs = objRefs;
                oriCollection.HardRefs = hardRefsToNestedOriCollections;
                // TODO DCE must be fired HERE  <---
                return oriCollection;
            }
            finally
            {
                EventDispatcher.FlushEventQueue();
            }
        }

        public IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            IdentityHashMap<IMergeServiceExtension, IList<Type>> mseToEntityTypes = new IdentityHashMap<IMergeServiceExtension, IList<Type>>();

		    for (int a = entityTypes.Count; a-- > 0;)
		    {
			    Type entityType = entityTypes[a];
			    IMergeServiceExtension mergeServiceExtension = mergeServiceExtensions.GetExtension(entityType);
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

        protected IDictionary<Type, IList<IChangeContainer>> BucketSortChanges(IList<IChangeContainer> allChanges)
        {
            IDictionary<Type, IList<IChangeContainer>> sortedChanges = new Dictionary<Type, IList<IChangeContainer>>();

            for (int i = allChanges.Count; i-- > 0; )
            {
                IChangeContainer changeContainer = allChanges[i];
                IObjRef objRef = changeContainer.Reference;
                Type type = objRef.RealType;
                IList<IChangeContainer> changeContainers = DictionaryExtension.ValueOrDefault(sortedChanges, type);
                if (changeContainers == null)
                {
                    changeContainers = new List<IChangeContainer>();
                    sortedChanges.Add(type, changeContainers);
                }
                changeContainers.Add(changeContainer);
            }
            return sortedChanges;
        }

        protected IList<MergeOperation> CreateMergeOperationSequence(IDictionary<Type, IList<IChangeContainer>> sortedChanges)
        {
            Type[] entityPersistOrder = EntityMetaDataProvider.GetEntityPersistOrder();
            IList<MergeOperation> mergeOperations = new List<MergeOperation>();
            
            if (entityPersistOrder != null)
            {
                for (int a = entityPersistOrder.Length; a-- > 0; )
                {
                    Type orderedEntityType = entityPersistOrder[a];
                    IList<IChangeContainer> changes = DictionaryExtension.ValueOrDefault(sortedChanges, orderedEntityType);
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
                        sortedChanges[orderedEntityType] = insertsAndUpdates;
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
                    IList<IChangeContainer> changes = DictionaryExtension.ValueOrDefault(sortedChanges, orderedEntityType);
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
            DictionaryExtension.Loop(sortedChanges, delegate(Type type, IList<IChangeContainer> unorderedChanges)
            {
                IMergeServiceExtension mergeServiceExtension = GetServiceForType(type);

                foreach (MergeOperation existingMergeOperation in mergeOperations)
                {
                    if (Object.ReferenceEquals(existingMergeOperation.MergeServiceExtension, mergeServiceExtension))
                    {
                        IList<IChangeContainer> orderedChanges = existingMergeOperation.ChangeContainer;
                        for (int b = unorderedChanges.Count; b-- > 0; )
                        {
                            orderedChanges.Add(unorderedChanges[b]);
                        }
                        return;
                    }
                }
                MergeOperation mergeOperation = new MergeOperation();
                mergeOperation.MergeServiceExtension = mergeServiceExtension;
                mergeOperation.ChangeContainer = unorderedChanges;

                mergeOperations.Add(mergeOperation);
            });
            return mergeOperations;
        }

        protected virtual void PostProcessOriCollection(ICUDResult cudResult, IOriCollection oriCollection)
        {
            GuiThreadHelper.InvokeInGuiAndWait(delegate()
            {
                MergeController.ApplyChangesToOriginals(cudResult.GetOriginalRefs(), oriCollection.AllChangeORIs, oriCollection.ChangedOn, oriCollection.ChangedBy);
            });
        }
    }
}
