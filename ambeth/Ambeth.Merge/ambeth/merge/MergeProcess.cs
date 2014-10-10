using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Merge
{
    public class MergeProcess : IMergeProcess
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        [Autowired]
        public ICache Cache { protected get; set; }

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        [Autowired]
        public IEventDispatcher EventDispatcher { protected get; set; }

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IMergeController MergeController { protected get; set; }

        [Autowired]
        public IMergeService MergeService { protected get; set; }

        [Autowired]
        public IObjRefHelper OriHelper { protected get; set; }

        [Autowired]
        public IRevertChangesHelper RevertChangesHelper { protected get; set; }

        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public bool IsNetworkClientMode { protected get; set; }

        public void Process(Object objectToMerge, Object objectToDelete, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback)
        {
            Process(objectToMerge, objectToDelete, proceedHook, mergeFinishedCallback, true);
        }

        public void Process(Object objectToMerge, Object objectToDelete, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback, bool addNewEntitiesToCache)
        {
            if (GuiThreadHelper.IsInGuiThread())
            {
                GuiThreadHelper.InvokeOutOfGui(delegate()
                {
                    MergePhase1(objectToMerge, objectToDelete, proceedHook, mergeFinishedCallback, addNewEntitiesToCache);
                });
            }
            else
            {
                MergePhase1(objectToMerge, objectToDelete, proceedHook, mergeFinishedCallback, addNewEntitiesToCache);
            }
        }

        protected void MergePhase1(Object objectToMerge, Object objectToDelete, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback, bool addNewEntitiesToCache)
        {
            IDisposableCache childCache = CacheFactory.Create(CacheFactoryDirective.NoDCE, false, false);
            try
            {
                MergeHandle mergeHandle = BeanContext.RegisterAnonymousBean<MergeHandle>().PropertyValue("Cache", childCache).Finish();
                ICUDResult cudResult = MergeController.MergeDeep(objectToMerge, mergeHandle);
                if (GuiThreadHelper.IsInGuiThread())
                {
                    MergePhase2(objectToMerge, objectToDelete, mergeHandle, cudResult, proceedHook, mergeFinishedCallback, addNewEntitiesToCache);
                }
                else
                {
                    GuiThreadHelper.InvokeInGui(delegate()
                    {
                        MergePhase2(objectToMerge, objectToDelete, mergeHandle, cudResult, proceedHook, mergeFinishedCallback, addNewEntitiesToCache);
                    });
                }
            }
            finally
            {
                childCache.Dispose();
            }
        }

        protected void MergePhase2(Object objectToMerge, Object objectToDelete, MergeHandle mergeHandle, ICUDResult cudResult, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback, bool addNewEntitiesToCache)
        {
            List<Object> unpersistedObjectsToDelete = new List<Object>();
            RemoveUnpersistedDeletedObjectsFromCudResult(cudResult.AllChanges, cudResult.GetOriginalRefs(), unpersistedObjectsToDelete);
            if (objectToDelete != null)
            {
                IList<IObjRef> oriList = OriHelper.ExtractObjRefList(objectToDelete, mergeHandle);

                AppendDeleteContainers(objectToDelete, oriList, cudResult.AllChanges, cudResult.GetOriginalRefs(), unpersistedObjectsToDelete);
            }

            // Store the MergeFinishedCallback from this thread on the stack and set the property null (for following calls):
            if (GuiThreadHelper.IsInGuiThread())
            {
                GuiThreadHelper.InvokeOutOfGui(delegate()
                {
                    MergePhase3(objectToMerge, unpersistedObjectsToDelete, cudResult, proceedHook, mergeFinishedCallback, addNewEntitiesToCache);
                });
            }
            else
            {
                MergePhase3(objectToMerge, unpersistedObjectsToDelete, cudResult, proceedHook, mergeFinishedCallback, addNewEntitiesToCache);
            }
        }

        protected void MergePhase3(Object objectToMerge, IList<Object> unpersistedObjectsToDelete, ICUDResult cudResult, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback,
            bool addNewEntitiesToCache)
        {
            // Take over callback stored threadlocally from foreign calling thread to current thread
            bool success = false;
            try
            {
                ProcessCUDResult(objectToMerge, cudResult, unpersistedObjectsToDelete, proceedHook, addNewEntitiesToCache);
                success = true;
            }
            finally
            {
                if (mergeFinishedCallback != null)
                {
                    mergeFinishedCallback.Invoke(success);
                }
            }
        }

        protected virtual void RemoveUnpersistedDeletedObjectsFromCudResult(IList<IChangeContainer> allChanges, IList<Object> originalRefs, IList<Object> unpersistedObjectsToDelete)
        {
            ISet<IObjRef> removedDirectObjRefs = null;
            for (int a = allChanges.Count; a-- > 0; )
            {
                IChangeContainer changeContainer = allChanges[a];
                IObjRef objRef = changeContainer.Reference;
                if (!(changeContainer is DeleteContainer) || objRef.Id != null)
                {
                    continue;
                }
                if (removedDirectObjRefs == null)
                {
                    removedDirectObjRefs = new IdentityHashSet<IObjRef>();
                }
                IDirectObjRef dirObjRef = (IDirectObjRef)objRef;
                // These are objects without an id but are marked as deleted. They will be deleted locally without transfer to the service
                allChanges.RemoveAt(a);
                originalRefs.RemoveAt(a);
                unpersistedObjectsToDelete.Add(dirObjRef.Direct);
                removedDirectObjRefs.Add(dirObjRef);
            }
            if (removedDirectObjRefs == null)
            {
                return;
            }
            // Scan all other changeContainer if they refer to the removed DeleteContainers of unpersisted entities
            for (int a = allChanges.Count; a-- > 0; )
            {
                IChangeContainer changeContainer = allChanges[a];
                IRelationUpdateItem[] relations;
                if (changeContainer is CreateContainer)
                {
                    relations = ((CreateContainer)changeContainer).Relations;
                }
                else if (changeContainer is UpdateContainer)
                {
                    relations = ((UpdateContainer)changeContainer).Relations;
                }
                else
                {
                    // DeleteContainers can not refer anything beside themselves
                    continue;
                }
                if (relations == null)
                {
                    continue;
                }
                for (int b = relations.Length; b-- > 0; )
                {
                    IRelationUpdateItem childItem = relations[b];
                    IObjRef[] addedOris = childItem.AddedORIs;
                    if (addedOris == null)
                    {
                        continue;
                    }
                    for (int c = addedOris.Length; c-- > 0; )
                    {
                        IObjRef addedOri = addedOris[c];
                        if (!removedDirectObjRefs.Contains(addedOri))
                        {
                            continue;
                        }
                        if (addedOris.Length == 1)
                        {
                            if (childItem.RemovedORIs != null)
                            {
                                ((RelationUpdateItem)childItem).AddedORIs = null;
                            }
                            else
                            {
                                if (relations.Length == 1)
                                {
                                    allChanges.RemoveAt(a);
                                    originalRefs.RemoveAt(a);
                                    relations = null;
                                    break;
                                }
                                IRelationUpdateItem[] newChildItems = new IRelationUpdateItem[relations.Length - 1];
                                Array.Copy(relations, 0, newChildItems, 0, b);
                                Array.Copy(relations, b + 1, newChildItems, b, relations.Length - b - 1);
                                relations = newChildItems;
                                if (changeContainer is CreateContainer)
                                {
                                    ((CreateContainer)changeContainer).Relations = relations;
                                }
                                else
                                {
                                    ((UpdateContainer)changeContainer).Relations = relations;
                                }
                            }
                            break;
                        }
                        IObjRef[] newAddedOris = new IObjRef[addedOris.Length - 1];
                        Array.Copy(addedOris, 0, newAddedOris, 0, c);
                        Array.Copy(addedOris, c + 1, newAddedOris, c, addedOris.Length - c - 1);
                        addedOris = newAddedOris;
                        ((RelationUpdateItem)childItem).AddedORIs = addedOris;
                    }
                    if (relations == null)
                    {
                        break;
                    }
                }
            }
        }

        protected virtual void AppendDeleteContainers(Object argument, IList<IObjRef> oriList,
           IList<IChangeContainer> allChanges, IList<Object> originalRefs, IList<Object> unpersistedObjectsToDelete)
        {
            if (argument is IEnumerable)
            {
                IEnumerator iter = ((IEnumerable)argument).GetEnumerator();
                for (int a = 0, size = oriList.Count; a < size; a++)
                {
                    iter.MoveNext();

                    IObjRef ori = oriList[a];
                    if (ori.Id == null)
                    {
                        unpersistedObjectsToDelete.Add(iter.Current);
                        continue;
                    }
                    DeleteContainer deleteContainer = new DeleteContainer();
                    deleteContainer.Reference = ori;

                    allChanges.Add(deleteContainer);
                    originalRefs.Add(iter.Current);
                }
            }
            else
            {
                IObjRef ori = oriList[0];
                if (ori.Id == null)
                {
                    unpersistedObjectsToDelete.Add(argument);
                    return;
                }
                DeleteContainer deleteContainer = new DeleteContainer();
                deleteContainer.Reference = ori;

                allChanges.Add(deleteContainer);
                originalRefs.Add(argument);
            }
        }

        protected virtual void ProcessCUDResult(Object objectToMerge, ICUDResult cudResult, IList<Object> unpersistedObjectsToDelete,
            ProceedWithMergeHook proceedHook, bool addNewEntitiesToCache)
        {
            if (cudResult.AllChanges.Count > 0 || (unpersistedObjectsToDelete != null && unpersistedObjectsToDelete.Count > 0))
            {
                if (proceedHook != null)
                {
                    bool proceed = proceedHook.Invoke(cudResult, unpersistedObjectsToDelete);
                    if (!proceed)
                    {
                        return;
                    }
                }
            }
            if (cudResult.AllChanges.Count == 0)
            {
                if (Log.InfoEnabled)
                {
                    Log.Info("Service call skipped early because there is nothing to merge");
                }
            }
            else
            {
                IOriCollection oriColl;
                EventDispatcher.EnableEventQueue();
                try
                {
                    EventDispatcher.Pause(Cache);
                    try
                    {
                        oriColl = MergeService.Merge(cudResult, null);
                        if (GuiThreadHelper.IsInGuiThread())
                        {
                            MergeController.ApplyChangesToOriginals(cudResult.GetOriginalRefs(), oriColl.AllChangeORIs, oriColl.ChangedOn, oriColl.ChangedBy);
                        }
                        else
                        {
                            GuiThreadHelper.InvokeInGuiAndWait(delegate()
                            {
                                MergeController.ApplyChangesToOriginals(cudResult.GetOriginalRefs(), oriColl.AllChangeORIs, oriColl.ChangedOn, oriColl.ChangedBy);
                            });
                        }
                    }
                    finally
                    {
                        EventDispatcher.Resume(Cache);
                    }
                }
                finally
                {
                    EventDispatcher.FlushEventQueue();
                }
                DataChangeEvent dataChange = DataChangeEvent.Create(-1, -1, -1);
                // This is intentionally a remote source
                dataChange.IsLocalSource = false;

                if (IsNetworkClientMode)
                {
                    IList<IChangeContainer> allChanges = cudResult.AllChanges;

                    IList<IObjRef> orisInReturn = oriColl.AllChangeORIs;
                    for (int a = allChanges.Count; a-- > 0; )
                    {
                        IChangeContainer changeContainer = allChanges[a];
                        IObjRef reference = changeContainer.Reference;
                        IObjRef referenceInReturn = orisInReturn[a];
                        if (changeContainer is CreateContainer)
                        {
                            if (referenceInReturn.IdNameIndex != ObjRef.PRIMARY_KEY_INDEX)
                            {
                                throw new ArgumentException("Implementation error: Only PK references are allowed in events");
                            }
                            dataChange.Inserts.Add(new DataChangeEntry(referenceInReturn.RealType, referenceInReturn.IdNameIndex, referenceInReturn.Id, referenceInReturn.Version));
                        }
                        else if (changeContainer is UpdateContainer)
                        {
                            if (referenceInReturn.IdNameIndex != ObjRef.PRIMARY_KEY_INDEX)
                            {
                                throw new ArgumentException("Implementation error: Only PK references are allowed in events");
                            }
                            dataChange.Updates.Add(new DataChangeEntry(referenceInReturn.RealType, referenceInReturn.IdNameIndex, referenceInReturn.Id, referenceInReturn.Version));
                        }
                        else if (changeContainer is DeleteContainer)
                        {
                            if (reference.IdNameIndex != ObjRef.PRIMARY_KEY_INDEX)
                            {
                                throw new ArgumentException("Implementation error: Only PK references are allowed in events");
                            }
                            dataChange.Deletes.Add(new DataChangeEntry(reference.RealType, reference.IdNameIndex, reference.Id, reference.Version));
                        }
                    }
                    //EventDispatcher.DispatchEvent(dataChange, DateTime.Now, -1);
                }
            }
            if (unpersistedObjectsToDelete != null && unpersistedObjectsToDelete.Count > 0)
            {
                // Create a DCE for all objects without an id but which should be deleted...
                // This is the case for newly created objects on client side, which should be
                // "cancelled". The DCE notifies all models which contain identity references to the related
                // objects to erase their existence in all controls. They are not relevant in the previous
                // server merge process
                DataChangeEvent dataChange = DataChangeEvent.Create(0, 0, unpersistedObjectsToDelete.Count);

                for (int a = unpersistedObjectsToDelete.Count; a-- > 0; )
                {
                    Object unpersistedObject = unpersistedObjectsToDelete[a];
                    dataChange.Deletes.Add(new DirectDataChangeEntry(unpersistedObject));
                }
                EventDispatcher.DispatchEvent(dataChange, DateTime.Now, -1);
            }
            RevertChangesHelper.RevertChanges(objectToMerge);
        }
    }
}
