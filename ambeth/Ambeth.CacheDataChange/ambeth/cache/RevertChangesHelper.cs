using System;
using System.Collections;
using System.Collections.Generic;

#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Cache
{
    public class RevertChangesHelper : IRevertChangesHelper, IInitializingBean
    {
        public IServiceContext BeanContext { protected get; set; }

        public ICacheModification CacheModification { protected get; set; }

        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public IEventDispatcher EventDispatcher { protected get; set; }

        public IFirstLevelCacheManager FirstLevelCacheManager { protected get; set; }

        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        public IMergeController MergeController { protected get; set; }

        public IProxyHelper ProxyHelper { protected get; set; }

        public RootCache RootCache { protected get; set; }

        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        protected readonly Lock readLock, writeLock;

        public RevertChangesHelper()
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(CacheModification, "CacheModification");
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
            ParamChecker.AssertNotNull(EventDispatcher, "EventDispatcher");
            ParamChecker.AssertNotNull(FirstLevelCacheManager, "FirstLevelCacheManager");
            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
            ParamChecker.AssertNotNull(MergeController, "MergeController");
            ParamChecker.AssertNotNull(ProxyHelper, "ProxyHelper");
            ParamChecker.AssertNotNull(RootCache, "RootCache");
            ParamChecker.AssertNotNull(TypeInfoProvider, "TypeInfoProvider");
        }

        public virtual IRevertChangesSavepoint CreateSavepoint(Object source)
        {
            if (source == null)
            {
                return null;
            }
            List<Object> objList = new List<Object>();
            List<IObjRef> objRefs = new List<IObjRef>();
            FindAllObjectsToBackup(source, objList, objRefs, new IdentityHashSet<Object>());

            IDictionary<Object, RevertChangesSavepoint.IBackup> originalToValueBackup = new IdentityDictionary<Object, RevertChangesSavepoint.IBackup>();

            // Iterate manually through the list because the list itself should not be 'backuped'
            for (int a = objList.Count; a-- > 0; )
            {
                BackupObjects(objList[a], originalToValueBackup);
            }
            WeakDictionary<Object, RevertChangesSavepoint.IBackup> weakObjectsToBackup = new WeakDictionary<Object, RevertChangesSavepoint.IBackup>(new IdentityEqualityComparer<Object>());
            DictionaryExtension.Loop(originalToValueBackup, delegate(Object obj, RevertChangesSavepoint.IBackup backup)
            {
                if (backup != null)
                {
                    weakObjectsToBackup.Add(obj, backup);
                }
            });

            return BeanContext.RegisterAnonymousBean<RevertChangesSavepoint>().PropertyValue("Changes", weakObjectsToBackup).Finish();
        }

        protected void BackupObjects(Object obj, IDictionary<Object, RevertChangesSavepoint.IBackup> originalToValueBackup)
        {
            if (obj == null)
            {
                return;
            }
            Type objType = ProxyHelper.GetRealType(obj.GetType());
            if (ImmutableTypeSet.IsImmutableType(objType) || originalToValueBackup.ContainsKey(obj))
            {
                return;
            }
            originalToValueBackup.Add(obj, null);
            if (obj is Array)
            {
                Array sourceArray = (Array)obj;
                RevertChangesSavepoint.ArrayBackup arrayBackup = new RevertChangesSavepoint.ArrayBackup((Array)sourceArray.Clone());
                originalToValueBackup[obj] = arrayBackup;
                Type elementType = sourceArray.GetType().GetElementType();
                if (!ImmutableTypeSet.IsImmutableType(elementType))
                {
                    for (int a = sourceArray.Length; a-- > 0; )
                    {
                        Object arrayItem = sourceArray.GetValue(a);
                        BackupObjects(arrayItem, originalToValueBackup);
                    }
                }
                return;
            }
            if (obj is IList)
            {
                IList list = (IList)obj;
                Object[] array = new Object[list.Count];
                list.CopyTo(array, 0);
                RevertChangesSavepoint.ListBackup listBackup = new RevertChangesSavepoint.ListBackup(array);
                originalToValueBackup[obj] = listBackup;
                for (int a = list.Count; a-- > 0; )
                {
                    Object item = list[a];
                    BackupObjects(item, originalToValueBackup);
                }
                return;
            }
            else if (obj is IEnumerable)
            {
                foreach (Object item in (IEnumerable)obj)
                {
                    BackupObjects(item, originalToValueBackup);
                }
                return;
            }
            ITypeInfo typeInfo = TypeInfoProvider.GetTypeInfo(objType);

            ITypeInfoItem[] members = typeInfo.Members;
            Object[] originalValues = new Object[members.Length];
            RevertChangesSavepoint.ObjectBackup objBackup = new RevertChangesSavepoint.ObjectBackup(members, originalValues);
            originalToValueBackup[obj] = objBackup;

            for (int b = members.Length; b-- > 0; )
            {
                ITypeInfoItem member = members[b];
                Object originalValue = member.GetValue(obj);
                originalValues[b] = originalValue;

                BackupObjects(originalValue, originalToValueBackup);
            }
        }

        protected void FindAllObjectsToBackup(Object obj, IList<Object> objList, IList<IObjRef> objRefs, ISet<Object> alreadyProcessedSet)
        {
            if (obj == null || !alreadyProcessedSet.Add(obj))
            {
                return;
            }
            // In java there has to be checked (in addition) for array-instance, too
            if (obj is IList)
            {
                IList list = (IList)obj;
                for (int a = list.Count; a-- > 0; )
                {
                    Object item = list[a];
                    FindAllObjectsToBackup(item, objList, objRefs, alreadyProcessedSet);
                }
                return;
            }
            else if (obj is IEnumerable)
            {
                IEnumerator iter = ((IEnumerable)obj).GetEnumerator();
                while (iter.MoveNext())
                {
                    Object item = iter.Current;
                    FindAllObjectsToBackup(item, objList, objRefs, alreadyProcessedSet);
                }
                return;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(obj.GetType());
            Object id = metaData.IdMember.GetValue(obj);
            objList.Add(obj);
            objRefs.Add(new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null));
            ITypeInfoItem[] relationMembers = metaData.RelationMembers;
            for (int a = relationMembers.Length; a-- > 0; )
            {
                ITypeInfoItem relationMember = relationMembers[a];
                Object item = relationMember.GetValue(obj);
                FindAllObjectsToBackup(item, objList, objRefs, alreadyProcessedSet);
            }
        }

        public virtual void RevertChanges(Object objectsToRevert)
        {
            RevertChanges(objectsToRevert, null, false);
        }

        public void RevertChanges(object objectsToRevert, bool recursive)
        {
            RevertChanges(objectsToRevert, null, recursive);
        }

        public virtual void RevertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback)
        {
            RevertChanges(objectsToRevert, revertChangesFinishedCallback, false);
        }

        public virtual void RevertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, bool recursive)
        {
            if (objectsToRevert == null)
            {
                return;
            }
            List<Object> revertList = new List<Object>();
            FillRevertList(objectsToRevert, new IdentityHashSet<Object>(), revertList, recursive);
            RevertChangesIntern(null, revertList, false, revertChangesFinishedCallback);
        }

        public virtual void RevertChangesGlobally(Object objectsToRevert)
        {
            RevertChangesGlobally(objectsToRevert, null, false);
        }

        public virtual void RevertChangesGlobally(Object objectsToRevert, bool recursive)
        {
            RevertChangesGlobally(objectsToRevert, null, recursive);
        }

        public virtual void RevertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback)
        {
            RevertChangesGlobally(objectsToRevert, revertChangesFinishedCallback, false);
        }

        public virtual void RevertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, bool recursive)
        {
            if (objectsToRevert == null)
            {
                return;
            }
            List<Object> revertList = new List<Object>();
            FillRevertList(objectsToRevert, new IdentityHashSet<Object>(), revertList, recursive);
            RevertChangesIntern(null, revertList, true, revertChangesFinishedCallback);
        }

        protected virtual void FillRevertList(Object obj, ISet<Object> alreadyScannedSet, IList<Object> revertList, bool recursive)
        {
            if (!alreadyScannedSet.Add(obj))
            {
                return;
            }
            if (obj is IList)
            {
                IList list = (IList)obj;
                for (int a = list.Count; a-- > 0; )
                {
                    FillRevertList(list[a], alreadyScannedSet, revertList, recursive);
                }
                return;
            }
            else if (obj is IEnumerable && !(obj is String))
            {
                IEnumerator iter = ((IEnumerable)obj).GetEnumerator();
                while (iter.MoveNext())
                {
                    FillRevertList(iter.Current, alreadyScannedSet, revertList, recursive);
                }
                return;
            }
            revertList.Add(obj);
            if (recursive)
            {
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(obj.GetType());
                IRelationInfoItem[] relations = metaData.RelationMembers;
                foreach (IRelationInfoItem relation in relations)
                {
                    Object value = relation.GetValue(obj);
                    FillRevertList(value, alreadyScannedSet, revertList, recursive);
                }
            }
        }

        protected virtual void RevertChangesIntern(IRevertChangesSavepoint savepoint, IList<Object> objectsToRevert, bool globally,
             RevertChangesFinishedCallback revertChangesFinishedCallback)
        {
            // Store the RevertChangesFinishedCallback from this thread on the stack and set the property null (for following calls):
            if (objectsToRevert == null || objectsToRevert.Count == 0)
            {
                if (revertChangesFinishedCallback != null)
                {
                    revertChangesFinishedCallback.Invoke(true);
                }
                return;
            }
            if (globally)
            {
                GuiThreadHelper.InvokeOutOfGui(delegate()
                {
                    bool success = false;
                    try
                    {
                        DataChangeEvent dataChange = new DataChangeEvent();
                        dataChange.IsLocalSource = true;
                        dataChange.Inserts = new List<IDataChangeEntry>(0);
                        dataChange.Updates = new List<IDataChangeEntry>();
                        dataChange.Deletes = new List<IDataChangeEntry>();

                        for (int a = objectsToRevert.Count; a-- > 0; )
                        {
                            Object objectToRevert = objectsToRevert[a];
                            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objectToRevert.GetType());
                            Object id = metaData.IdMember.GetValue(objectToRevert, false);

                            if (id == null)
                            {
                                dataChange.Deletes.Add(new DirectDataChangeEntry(objectToRevert));
                                continue;
                            }
                            dataChange.Updates.Add(new DataChangeEntry(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null));
                        }

                        EventDispatcher.DispatchEvent(dataChange, DateTime.Now, -1);
                        success = true;
                    }
                    finally
                    {
                        if (revertChangesFinishedCallback != null)
                        {
                            revertChangesFinishedCallback.Invoke(success);
                        }
                    }
                });
            }
            else
            {
                // Commented the following part from Ambeth 0.130 and use the part from Ambeth 0.129 due to a deadlock in the merge process:
                //GuiThreadHelper.InvokeOutOfGui(delegate()
                //{
                //    bool success1 = false;
                //    try
                //    {
                //        IList<IDataChangeEntry> directObjectDeletes = new List<IDataChangeEntry>();
                //        IList<Object> initializedObjects = MergeController.ScanForInitializedObjects(objectsToRevert, true, null);

                //        IList<IObjRef> orisToRevert = new List<IObjRef>();
                //        ISet<Object> persistedObjectsToRevert = new IdentityHashSet<Object>();
                //        for (int a = initializedObjects.Count; a-- > 0; )
                //        {
                //            Object objectToRevert = initializedObjects[a];
                //            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objectToRevert.GetType());
                //            Object id = metaData.IdMember.GetValue(objectToRevert, false);

                //            if (id == null)
                //            {
                //                directObjectDeletes.Add(new DirectDataChangeEntry(objectToRevert));
                //                continue;
                //            }
                //            persistedObjectsToRevert.Add(objectToRevert);
                //            orisToRevert.Add(new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null));
                //        }
                //        IList<Object> hardRefsToRootCacheValues = RootCache.GetObjects(orisToRevert, CacheDirective.CacheValueResult | CacheDirective.ReturnMisses);

                //        for (int a = orisToRevert.Count; a-- > 0; )
                //        {
                //            if (hardRefsToRootCacheValues[a] == null)
                //            {
                //                // Object could not be loaded/retrieved any more. So the ori refers to an invalid object
                //                // We can not revert invalid objects and currently ignore them. They will raise exceptions if they will
                //                // be tried to persist in a merge process any time in the future
                //                orisToRevert.RemoveAt(a);
                //            }
                //        }
                //        // We do nothing with the hardRef-list from the RootCache. It is only necessary to keep track of the instance reference on the stack
                //        // To prohibit GC any potential WeakReferences in the meantime....
                //        GuiThreadHelper.InvokeInGuiAndWait(delegate()
                //        {
                //            IProcessResumeItem processResumeItem = WaitEventToResume();
                //            try
                //            {
                //                bool oldCacheModificationValue = CacheModification.IsActive;
                //                CacheModification.IsActive = true;
                //                bool oldFailEarlyModeActive = AbstractCache<Object>.FailEarlyModeActive;
                //                AbstractCache<Object>.FailEarlyModeActive = true;
                //                try
                //                {
                //                    IList<IWritableCache> firstLevelCaches = FirstLevelCacheManager.SelectFirstLevelCaches();
                //                    IList<Object> hardRefsToRootCacheValuesHere = hardRefsToRootCacheValues;

                //                    foreach (IWritableCache firstLevelCache in firstLevelCaches)
                //                    {
                //                        IList<Object> persistedObjectsInThisCache = firstLevelCache.GetObjects(orisToRevert, CacheDirective.FailEarly);

                //                        for (int a = persistedObjectsInThisCache.Count; a-- > 0; )
                //                        {
                //                            Object persistedObjectInThisCache = persistedObjectsInThisCache[a];
                //                            if (!persistedObjectsToRevert.Contains(persistedObjectInThisCache))
                //                            {
                //                                continue;
                //                            }
                //                            RootCache.ApplyValues(persistedObjectInThisCache, (ICacheIntern)firstLevelCache);
                //                        }
                //                    }
                //                    for (int a = objectsToRevert.Count; a-- > 0; )
                //                    {
                //                        Object objectToRevert = objectsToRevert[a];
                //                        if (objectToRevert is IDataObject)
                //                        {
                //                            // Objects which are specified to be reverted loose their delete flag
                //                            ((IDataObject)objectToRevert).ToBeDeleted = false;
                //                        }
                //                    }
                //                }
                //                finally
                //                {
                //                    AbstractCache<Object>.FailEarlyModeActive = oldFailEarlyModeActive;
                //                    CacheModification.IsActive = oldCacheModificationValue;
                //                }
                //            }
                //            finally
                //            {
                //                if (processResumeItem != null)
                //                {
                //                    processResumeItem.ResumeProcessingFinished();
                //                    processResumeItem = null;
                //                }
                //            }
                //        });
                //        if (directObjectDeletes.Count > 0)
                //        {
                //            DataChangeEvent dataChange = DataChangeEvent.Create(0, 0, 0);
                //            dataChange.Deletes = directObjectDeletes;

                //            EventDispatcher.DispatchEvent(dataChange, DateTime.Now, -1);
                //        }
                //        success1 = true;
                //    }
                //    finally
                //    {
                //        if (revertChangesFinishedCallback != null)
                //        {
                //            revertChangesFinishedCallback.Invoke(success1);
                //        }
                //    }
                //});

                // Here comes the part from Ambeth 0.129:
                GuiThreadHelper.InvokeOutOfGui(delegate()
                {
                    bool success1 = false;
                    bool? success2 = null;
                    bool? success3 = null;
                    try
                    {
                        IList<IDataChangeEntry> directObjectDeletes = new List<IDataChangeEntry>();
                        List<IObjRef> objRefs = new List<IObjRef>();
                        List<ValueHolderRef> valueHolderKeys = new List<ValueHolderRef>();
                        IList<Object> initializedObjects = MergeController.ScanForInitializedObjects(objectsToRevert, true, null, objRefs, valueHolderKeys);

                        IList<IObjRef> orisToRevert = new List<IObjRef>();
                        ISet<Object> persistedObjectsToRevert = new IdentityHashSet<Object>();
                        for (int a = initializedObjects.Count; a-- > 0; )
                        {
                            Object objectToRevert = initializedObjects[a];
                            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objectToRevert.GetType());
                            Object id = metaData.IdMember.GetValue(objectToRevert, false);

                            if (id == null)
                            {
                                directObjectDeletes.Add(new DirectDataChangeEntry(objectToRevert));
                                continue;
                            }
                            persistedObjectsToRevert.Add(objectToRevert);
                            orisToRevert.Add(new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null));
                        }
                        IList<Object> hardRefsToRootCacheValues = RootCache.GetObjects(orisToRevert, CacheDirective.CacheValueResult | CacheDirective.ReturnMisses);

                        for (int a = orisToRevert.Count; a-- > 0; )
                        {
                            if (hardRefsToRootCacheValues[a] == null)
                            {
                                // Object could not be loaded/retrieved any more. So the ori refers to an invalid object
                                // We can not revert invalid objects and currently ignore them. They will raise exceptions if they will
                                // be tried to persist in a merge process any time in the future
                                orisToRevert.RemoveAt(a);
                            }
                        }
                        // We do nothing with the hardRef-list from the RootCache. It is only necessary to keep track of the instance reference on the stack
                        // To prohibit GC any potential WeakReferences in the meantime....
                        success2 = false;
                        GuiThreadHelper.InvokeInGui(delegate(Object state)
                        {
                            WaitEventToResume(delegate(IProcessResumeItem processResumeItem)
                            {
                                try
                                {
                                    bool oldCacheModificationValue = CacheModification.Active;
                                    CacheModification.Active = true;
                                    bool oldFailEarlyModeActive = AbstractCache<Object>.FailEarlyModeActive;
                                    AbstractCache<Object>.FailEarlyModeActive = true;
                                    try
                                    {
                                        IList<IWritableCache> firstLevelCaches = FirstLevelCacheManager.SelectFirstLevelCaches();
                                        IList<Object> hardRefsToRootCacheValuesHere = hardRefsToRootCacheValues;

                                        foreach (IWritableCache firstLevelCache in firstLevelCaches)
                                        {
                                            IList<Object> persistedObjectsInThisCache = firstLevelCache.GetObjects(orisToRevert, CacheDirective.FailEarly);

                                            for (int a = persistedObjectsInThisCache.Count; a-- > 0; )
                                            {
                                                Object persistedObjectInThisCache = persistedObjectsInThisCache[a];
                                                if (!persistedObjectsToRevert.Contains(persistedObjectInThisCache))
                                                {
                                                    continue;
                                                }
                                                RootCache.ApplyValues(persistedObjectInThisCache, (ICacheIntern)firstLevelCache);
                                            }
                                        }
                                        for (int a = objectsToRevert.Count; a-- > 0; )
                                        {
                                            Object objectToRevert = objectsToRevert[a];
                                            if (objectToRevert is IDataObject)
                                            {
                                                // Objects which are specified to be reverted loose their flags
                                                ((IDataObject)objectToRevert).ToBeDeleted = false;
                                            }
                                        }
                                        if (directObjectDeletes.Count == 0)
                                        {
                                            success2 = true;
                                            return;
                                        }
                                    }
                                    finally
                                    {
                                        AbstractCache<Object>.FailEarlyModeActive = oldFailEarlyModeActive;
                                        CacheModification.Active = oldCacheModificationValue;
                                    }
                                }
                                finally
                                {
                                    if (processResumeItem != null)
                                    {
                                        processResumeItem.ResumeProcessingFinished();
                                    }
                                }
                                success3 = false;
                                GuiThreadHelper.InvokeOutOfGui(delegate()
                                {
                                    try
                                    {
                                        DataChangeEvent dataChange = DataChangeEvent.Create(0, 0, 0);
                                        dataChange.Deletes = directObjectDeletes;

                                        EventDispatcher.DispatchEvent(dataChange, DateTime.Now, -1);
                                        success3 = true;
                                    }
                                    finally
                                    {
                                        if (revertChangesFinishedCallback != null)
                                        {
                                            revertChangesFinishedCallback.Invoke(success3.Value);
                                        }
                                    }
                                });
                                success2 = true;
                            }, delegate(Exception e)
                            {
                                if (revertChangesFinishedCallback != null && success3 == null)
                                {
                                    revertChangesFinishedCallback.Invoke(success2.Value);
                                }
                            });
                        }, null);
                        success1 = true;
                    }
                    finally
                    {
                        if (revertChangesFinishedCallback != null && success2 == null && success3 == null)
                        {
                            revertChangesFinishedCallback.Invoke(success1);
                        }
                    }
                });
            }
        }

        protected void WaitEventToResume(IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate, IBackgroundWorkerParamDelegate<Exception> errorDelegate)
        {
            IRootCache rootCache = RootCache;
            IList<IWritableCache> selectedFirstLevelCaches = FirstLevelCacheManager.SelectFirstLevelCaches();

            ISet<Object> collisionSet = new IdentityHashSet<Object>();
            collisionSet.Add(rootCache);
            for (int a = selectedFirstLevelCaches.Count; a-- > 0; )
            {
                collisionSet.Add(selectedFirstLevelCaches[a]);
            }
            // Without the current rootcache we can not handle the event now. We have to block till the rootCache and all childCaches get valid
            EventDispatcher.WaitEventToResume(collisionSet, -1, resumeDelegate, errorDelegate);
        }
    }
}
