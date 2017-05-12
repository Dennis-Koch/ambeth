using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Merge
{
    public class MergeController : IMergeController, IMergeExtendable
    {
        protected static readonly CacheDirective failEarlyAndReturnMissesSet = CacheDirective.FailEarly | CacheDirective.ReturnMisses;

        protected readonly IMapExtendableContainer<Type, IMergeExtension> mergeExtensions = new ClassExtendableContainer<IMergeExtension>("mergeExtension", "type");

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public ICacheProvider CacheProvider { protected get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }

        [Autowired]
        public ICUDResultHelper CUDResultHelper { protected get; set; }

        [Autowired]
        public IObjRefFactory ObjRefFactory { protected get; set; }

        [Autowired]
        public IObjRefHelper OriHelper { protected get; set; }

        [Autowired]
        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        [Property(MergeConfigurationConstants.ExactVersionForOptimisticLockingRequired, DefaultValue = "false")]
        public bool ExactVersionForOptimisticLockingRequired { protected get; set; }

        [Property(MergeConfigurationConstants.AlwaysUpdateVersionInChangedEntities, DefaultValue = "false")]
        public bool AlwaysUpdateVersionInChangedEntities { protected get; set; }
        
        public void RegisterMergeExtension(IMergeExtension mergeExtension, Type entityType)
        {
            mergeExtensions.Register(mergeExtension, entityType);
        }

        public void UnregisterMergeExtension(IMergeExtension mergeExtension, Type entityType)
        {
            mergeExtensions.Unregister(mergeExtension, entityType);
        }

        public void ApplyChangesToOriginals(ICUDResult cudResult, IOriCollection oriCollection, ICache cache)
	    {
		    if (GuiThreadHelper.IsInGuiThread())
		    {
			    ApplyChangesToOriginalsIntern(cudResult, oriCollection, cache);
			    return;
		    }
		    GuiThreadHelper.InvokeInGuiAndWait(new IBackgroundWorkerDelegate(delegate()
			    {
				    ApplyChangesToOriginalsIntern(cudResult, oriCollection, cache);
			    }));
	    }

	    protected void ApplyChangesToOriginalsIntern(ICUDResult cudResult, IOriCollection oriCollection, ICache cache)
	    {
            ICacheModification cacheModification = this.CacheModification;
            IConversionHelper conversionHelper = this.ConversionHelper;
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            IList<Object> originalRefs = cudResult.GetOriginalRefs();
            IList<IObjRef> allChangeORIs = oriCollection.AllChangeORIs;
            String[] allChangedBy = oriCollection.AllChangedBy;
            long[] allChangedOn = oriCollection.AllChangedOn;
            String singleChangedBy = oriCollection.ChangedBy;
            long? singleChangedOn = oriCollection.ChangedOn;

            bool newInstanceOnCall = CacheProvider.IsNewInstanceOnCall;
            IList<Object> validObjects = new List<Object>(originalRefs.Count);
            bool oldCacheModificationValue = CacheModification.Active;
            CacheModification.Active = true;
            try
            {
                for (int a = originalRefs.Count; a-- > 0; )
                {
                    Object originalRef = originalRefs[a];
                    IObjRef ori = allChangeORIs[a];

                    if (originalRef == null)
                    {
                        // Object has been deleted by cascade delete contraints on server merge or simply a "not specified" original ref
                        continue;
                    }
                    if (originalRef is IObjRef)
                    {
                        continue;
                    }
                    long? changedOn = allChangedOn != null ? allChangedOn[a] : singleChangedOn;
                    String changedBy = allChangedBy != null ? allChangedBy[a] : singleChangedBy;
                    IEntityMetaData metaData = ((IEntityMetaDataHolder) originalRef).Get__EntityMetaData();
                    PrimitiveMember versionMember = metaData.VersionMember;

                    PrimitiveMember keyMember = metaData.IdMember;

                    PrimitiveMember onMember, byMember;
                    if (keyMember.GetValue(originalRef, false) == null)
                    {
                        onMember = metaData.CreatedOnMember;
                        byMember = metaData.CreatedByMember;
                    }
                    else
                    {
                        onMember = metaData.UpdatedOnMember;
                        byMember = metaData.UpdatedByMember;
                    }
                    if (onMember != null && changedOn != null)
                    {
                        Object createdOn = ConversionHelper.ConvertValueToType(onMember.ElementType, changedOn);
                        onMember.SetValue(originalRef, createdOn);
                    }
                    if (byMember != null && changedBy != null)
                    {
                        Object createdBy = ConversionHelper.ConvertValueToType(byMember.ElementType, changedBy);
                        byMember.SetValue(originalRef, createdBy);
                    }
                    if (ori == null)
                    {
                        keyMember.SetValue(originalRef, null);
                        if (versionMember != null)
                        {
                            versionMember.SetValue(originalRef, null);
                        }
                        if (originalRef is IDataObject)
                        {
                            ((IDataObject)originalRef).ToBeUpdated = false;
                            ((IDataObject)originalRef).ToBeDeleted = false;
                        }
                        continue; // Object has been deleted directly
                    }
                    keyMember.SetValue(originalRef, ConversionHelper.ConvertValueToType(keyMember.RealType, ori.Id));
                    if (versionMember != null)
                    {
                        if (AlwaysUpdateVersionInChangedEntities)
                        {
                            versionMember.SetValue(originalRef, ConversionHelper.ConvertValueToType(versionMember.RealType, ori.Version));
                        }
                        else
                        {
                            // We INTENTIONALLY do NOT set the version and let it on its old value, to force the following DCE to refresh the cached object with 'real' data
                            // If we set the version here to the ori.getVersion(), the DCE will 'see' a already valid object - but is IS NOT valid
                            // because it may not contain bi-directional information which can only be resolved by reloading the object from persistence layer
                            //versionMember.SetValue(originalRef, null);
                        }
                    }
                    if (originalRef is IDataObject)
                    {
                        ((IDataObject)originalRef).ToBeUpdated = false;
                        ((IDataObject)originalRef).ToBeDeleted = false;
                    }
                    validObjects.Add(originalRef);
                }
                PutInstancesToCurrentCache(validObjects, cache);
            }
            finally
            {
                CacheModification.Active = oldCacheModificationValue;
            }
        }

        protected void PutInstancesToCurrentCache(IList<Object> validObjects, ICache cache)
        {
            if (!MergeProcess.IsAddNewlyPersistedEntities())
            {
                return;
            }
            IWritableCache currentCache = cache != null ? (IWritableCache)cache : (IWritableCache)CacheProvider.GetCurrentCache();
            currentCache.Put(validObjects);
        }

        public virtual ICUDResult MergeDeep(Object obj, MergeHandle handle)
        {
            ICache cache = handle.Cache;
            if (cache == null && CacheFactory != null)
            {
                cache = CacheFactory.Create(CacheFactoryDirective.NoDCE, false, false, "MergeController.ORIGINAL");
                handle.Cache = cache;
            }
            IMap<Type, IList<Object>> typeToObjectsToMerge = null;
            Type[] entityPersistOrder = EntityMetaDataProvider.GetEntityPersistOrder();
            if (entityPersistOrder != null && entityPersistOrder.Length > 0)
            {
                typeToObjectsToMerge = new HashMap<Type, IList<Object>>();
            }
            List<IObjRef> objRefs = new List<IObjRef>();
            List<ValueHolderRef> valueHolderKeys = new List<ValueHolderRef>();
            IList<Object> objectsToMerge = ScanForInitializedObjects(obj, handle.IsDeepMerge, typeToObjectsToMerge, objRefs, valueHolderKeys);
            IList<Object> eagerlyLoadedOriginals = null;
            // Load all requested object originals in one roundtrip
            if (objRefs.Count > 0)
            {
                eagerlyLoadedOriginals = cache.GetObjects(objRefs, CacheDirective.ReturnMisses);
                for (int a = eagerlyLoadedOriginals.Count; a-- > 0; )
                {
                    IObjRef existingOri = objRefs[a];
                    if (eagerlyLoadedOriginals[a] == null && existingOri != null && existingOri.Id != null)
                    {
                        // Cache miss for an entity we want to merge. This is an OptimisticLock-State
                        throw new OptimisticLockException(null, null, existingOri);
                    }
                }
                List<IObjRef> objRefsOfVhks = new List<IObjRef>(valueHolderKeys.Count);
                for (int a = 0, size = valueHolderKeys.Count; a < size; a++)
                {
                    objRefsOfVhks.Add(valueHolderKeys[a].ObjRef);
                }
                IList<Object> objectsOfVhks = cache.GetObjects(objRefsOfVhks, CacheDirective.FailEarly | CacheDirective.ReturnMisses);
				for (int a = valueHolderKeys.Count; a-- > 0; )
				{
					IObjRefContainer objectOfVhk = (IObjRefContainer)objectsOfVhks[a];
					if (objectOfVhk == null)
					{
						continue;
					}
					ValueHolderRef valueHolderRef = valueHolderKeys[a];
					if (ValueHolderState.INIT != objectOfVhk.Get__State(valueHolderRef.RelationIndex))
					{
						DirectValueHolderRef vhcKey = new DirectValueHolderRef(objectOfVhk, valueHolderRef.Member);
						handle.PendingValueHolders.Add(vhcKey);
					}
				}
			}
            if (typeToObjectsToMerge != null)
            {
                foreach (Type orderedEntityType in entityPersistOrder)
                {
                    IList<Object> objectsToMergeOfOrderedType = typeToObjectsToMerge.Remove(orderedEntityType);
                    if (objectsToMergeOfOrderedType == null)
                    {
                        continue;
                    }
                    MergeDeepStart(objectsToMergeOfOrderedType, handle);
                }
                foreach (Entry<Type, IList<Object>> entry in typeToObjectsToMerge)
                {
                    IList<Object> objectsToMergeOfUnorderedType = entry.Value;
                    MergeDeepStart(objectsToMergeOfUnorderedType, handle);
                }
            }
            else if (objectsToMerge.Count > 0)
            {
                MergeDeepStart(objectsToMerge, handle);
            }
            return CUDResultHelper.CreateCUDResult(handle);
        }

        public IList<Object> ScanForInitializedObjects(Object obj, bool isDeepMerge, IMap<Type, IList<Object>> typeToObjectsToMerge, IList<IObjRef> objRefs,
            IList<ValueHolderRef> valueHolderKeys)
        {
            IList<Object> objects = new List<Object>();
            ISet<Object> alreadyHandledObjectsSet = new IdentityHashSet<Object>();
            ScanForInitializedObjectsIntern(obj, isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
            return objects;
        }

        protected void ScanForInitializedObjectsIntern(Object obj, bool isDeepMerge, IList<Object> objects, IMap<Type, IList<Object>> typeToObjectsToMerge,
            ISet<Object> alreadyHandledObjectsSet, IList<IObjRef> objRefs, IList<ValueHolderRef> valueHolderKeys)
        {
            if (obj == null || !alreadyHandledObjectsSet.Add(obj))
            {
                return;
            }
            if (obj is IList)
            {
                IList list = (IList)obj;
                for (int a = 0, size = list.Count; a < size; a++)
                {
                    ScanForInitializedObjectsIntern(list[a], isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
                }
                return;
            }
            else if (obj.GetType().IsArray)
            {
                Array array = (Array)obj;
                for (int a = array.Length; a-- > 0; )
                {
                    Object item = array.GetValue(a);
                    ScanForInitializedObjectsIntern(item, isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
                }
                return;
            }
            else if (obj is IEnumerable && !(obj is String))
            {
                foreach (Object item in (IEnumerable)obj)
                {
                    ScanForInitializedObjectsIntern(item, isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
                }
                return;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(obj.GetType(), true);
            if (metaData == null)
            {
                return;
            }
            IObjRef objRef = null;
		    Object id = metaData.IdMember.GetValue(obj, false);
		    if (id != null)
		    {
                objRef = ObjRefFactory.CreateObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null);
				objRef.GetType().GetFields(BindingFlags.Instance | BindingFlags.NonPublic);
		    }
		    if (!(obj is IDataObject) || ((IDataObject) obj).HasPendingChanges)
		    {
			    if (typeToObjectsToMerge != null)
			    {
				    IList<Object> objectsToMerge = typeToObjectsToMerge.Get(metaData.EntityType);
				    if (objectsToMerge == null)
				    {
					    objectsToMerge = new List<Object>();
                        typeToObjectsToMerge.Put(metaData.EntityType, objectsToMerge);
				    }
				    objectsToMerge.Add(obj);
			    }
			    objects.Add(obj);
			    objRefs.Add(objRef);
		    }
		    if (!isDeepMerge)
		    {
			    return;
		    }
            RelationMember[] relationMembers = metaData.RelationMembers;
            if (relationMembers.Length == 0)
            {
                return;
            }
            IObjRefContainer vhc = (IObjRefContainer)obj;
            for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
            {
                if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                {
                    continue;
                }
				RelationMember relationMember = relationMembers[relationIndex];
				Object item = relationMember.GetValue(obj);
                if (objRef != null && item != null)
                {
					ValueHolderRef vhk = new ValueHolderRef(objRef, relationMember, relationIndex);
                    valueHolderKeys.Add(vhk);
                }
                ScanForInitializedObjectsIntern(item, isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
            }
        }

        protected void MergeDeepStart(Object obj, MergeHandle handle)
        {
            if (handle.PendingValueHolders.Count > 0)
            {
                IList<Object> pendingValueHolders = handle.PendingValueHolders;
                PrefetchHelper.Prefetch(pendingValueHolders);
                pendingValueHolders.Clear();
            }
            MergeDeepIntern(obj, handle);

            while (true)
            {
                IList<IBackgroundWorkerDelegate> pendingRunnables = handle.PendingRunnables;
                IList<Object> pendingValueHolders = handle.PendingValueHolders;
                if (pendingValueHolders.Count == 0 && pendingRunnables.Count == 0)
                {
                    return;
                }
                if (pendingValueHolders.Count > 0)
                {
                    PrefetchHelper.Prefetch(pendingValueHolders);
                    pendingValueHolders.Clear();
                }
                if (pendingRunnables.Count > 0)
                {
                    IList<IBackgroundWorkerDelegate> pendingRunnablesClone = new List<IBackgroundWorkerDelegate>(pendingRunnables);
                    for (int a = 0, size = pendingRunnablesClone.Count; a < size; a++)
                    {
                        pendingRunnablesClone[a].Invoke();
                    }
                }
            }
        }

        protected void MergeDeepIntern(Object obj, MergeHandle handle)
        {
            if (obj == null)
            {
                return;
            }
            if (obj is IList)
            {
                if (!handle.alreadyProcessedSet.Add(obj))
                {
                    return;
                }
                IList objList = (IList)obj;
                for (int a = 0, size = objList.Count; a < size; a++)
                {
                    MergeDeepIntern(objList[a], handle);
                }
            }
            else if (obj is IEnumerable)
            {
                if (!handle.alreadyProcessedSet.Add(obj))
                {
                    return;
                }
                IEnumerator objEnumerator = ((IEnumerable)obj).GetEnumerator();
                while (objEnumerator.MoveNext())
                {
                    MergeDeepIntern(objEnumerator.Current, handle);
                }
            }
            else
            {
                MergeOrPersist(obj, handle);
            }
        }

        protected virtual void MergeOrPersist(Object obj, MergeHandle handle)
        {
            if (obj == null || !handle.alreadyProcessedSet.Add(obj))
            {
                return;
            }
            if (obj is IDataObject)
            {
                IDataObject dataObject = (IDataObject)obj;
                if (!dataObject.HasPendingChanges)
                {
                    return;
                }
                if (dataObject.ToBeDeleted)
                {
                    handle.objToDeleteSet.Add(obj);
                    return;
                }
            }
            IEntityMetaData metaData = ((IEntityMetaDataHolder)obj).Get__EntityMetaData();
            metaData.PrePersist(obj);
            Object key = metaData.IdMember.GetValue(obj, false);
            if (key == null)
            {
                Persist(obj, handle);
                return;
            }
            if (handle.Cache == null)
            {
                throw new Exception("Object has been cloned somewhere");
            }
            Object clone = handle.Cache.GetObject(metaData.EntityType, key);
            if (clone == null)
            {
                throw new OptimisticLockException(null, metaData.VersionMember != null ? metaData.VersionMember.GetValue(obj, false) : null, obj);
            }
            Merge(obj, clone, handle);
        }

        protected virtual void Persist(Object obj, MergeHandle handle)
        {
            IEntityMetaData metaData = ((IEntityMetaDataHolder)obj).Get__EntityMetaData();

            AddModification(obj, handle); // Ensure entity will be persisted even if no single property is specified

            RelationMember[] relationMembers = metaData.RelationMembers;
            if (relationMembers.Length > 0)
            {
                IObjRefContainer vhc = (IObjRefContainer)obj;
                for (int relationIndex = relationMembers.Length; relationIndex-- > 0;)
                {
                    RelationMember relationMember = relationMembers[relationIndex];
                    if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                    {
                        continue;
                    }
                    Object objMember = relationMember.GetValue(obj, false);

                    if (objMember == null)
                    {
                        continue;
                    }
                    AddOriModification(obj, relationMember.Name, objMember, null, handle);
                }
            }
            foreach (PrimitiveMember primitiveMember in metaData.PrimitiveMembers)
            {
                if (primitiveMember.TechnicalMember)
                {
                    continue;
                }
                Object objMember = primitiveMember.GetValue(obj, true);

                if (objMember != null)
                {
                    AddModification(obj, primitiveMember.Name, primitiveMember.ElementType, objMember, null, handle);
                }
            }
        }

        protected virtual void Merge(Object obj, Object clone, MergeHandle handle)
        {
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            IEntityMetaData metaData = ((IEntityMetaDataHolder)obj).Get__EntityMetaData();

            bool fieldBasedMergeActive = handle.FieldBasedMergeActive;
            bool oneChangeOccured = false;
            try
            {
                RelationMember[] relationMembers = metaData.RelationMembers;
                if (relationMembers.Length > 0)
                {
                    IObjRefContainer vhc = (IObjRefContainer)obj;
                    for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
                    {
                        RelationMember relationMember = relationMembers[relationIndex];
                        if (!metaData.IsMergeRelevant(relationMember))
                        {
                            continue;
                        }
                        if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                        {
                            // v2 valueholder is not initialized. so a change is impossible
                            continue;
                        }
                        Object objMember = relationMember.GetValue(obj, false);
                        Object cloneMember = relationMember.GetValue(clone, false);
                        if (objMember is IDataObject && !((IDataObject)objMember).HasPendingChanges)
                        {
                            IEntityMetaData relationMetaData = entityMetaDataProvider.GetMetaData(relationMember.RealType);
                            if (EqualsReferenceOrId(objMember, cloneMember, handle, relationMetaData))
                            {
                                continue;
                            }
                        }

                        IEntityMetaData childMetaData = entityMetaDataProvider.GetMetaData(relationMember.ElementType);

                        if (IsMemberModified(objMember, cloneMember, handle, childMetaData))
                        {
                            oneChangeOccured = true;
                            AddOriModification(obj, relationMember.Name, objMember, cloneMember, handle);
                        }
                    }
                }
                if (fieldBasedMergeActive)
                {
                    MergePrimitivesFieldBased(metaData, obj, clone, handle);
                    return;
                }
                bool additionalRound;
                do
                {
                    additionalRound = !oneChangeOccured;
                    foreach (PrimitiveMember primitiveMember in metaData.PrimitiveMembers)
                    {
                        if (!metaData.IsMergeRelevant(primitiveMember))
                        {
                            continue;
                        }
                        Object objValue = primitiveMember.GetValue(obj, true);
                        if (oneChangeOccured)
                        {
                            AddModification(obj, primitiveMember.Name, primitiveMember.ElementType, objValue, null, handle);
                            continue;
                        }
                        Object cloneValue = primitiveMember.GetValue(clone, true);
                        if (!ArePrimitivesEqual(metaData, primitiveMember, objValue, cloneValue, handle))
                        {
                            oneChangeOccured = true;
                            break;
                        }
                    }
                }
                while (additionalRound && oneChangeOccured);
            }
            finally
            {
                PrimitiveMember versionMember = metaData.VersionMember;
                if (oneChangeOccured && versionMember !=null)
                {
                    // Check for early optimistic locking (Another, later level is directly on persistence layer)
                    Object versionToMerge = versionMember.GetValue(obj, true);
                    Object currentVersion = versionMember.GetValue(clone, true);

                    int compareResult = ((IComparable)versionToMerge).CompareTo(currentVersion);
                    if (ExactVersionForOptimisticLockingRequired ? compareResult != 0 : compareResult < 0)
                    {
                        throw new OptimisticLockException(currentVersion, versionToMerge, obj);
                    }
                }
            }
        }

        protected void MergePrimitivesFieldBased(IEntityMetaData metaData, Object obj, Object clone, MergeHandle handle)
        {
            foreach (PrimitiveMember primitiveMember in metaData.PrimitiveMembers)
            {
                if (!metaData.IsMergeRelevant(primitiveMember))
                {
                    continue;
                }
                Object objValue = primitiveMember.GetValue(obj, true);
                Object cloneValue = primitiveMember.GetValue(clone, true);
                if (!ArePrimitivesEqual(metaData, primitiveMember, objValue, cloneValue, handle))
                {
                    AddModification(obj, primitiveMember.Name, primitiveMember.ElementType, objValue, cloneValue, handle);
                }
            }
        }

        protected bool ArePrimitivesEqual(IEntityMetaData metaData, PrimitiveMember primitiveMember, Object objValue, Object cloneValue, MergeHandle handle)
        {
            if (objValue != null && cloneValue != null)
            {
                if (objValue is Array && cloneValue is Array)
                {
                    Array objArray = (Array)objValue;
                    Array cloneArray = (Array)cloneValue;
                    if (objArray.Length != cloneArray.Length)
                    {
                        return false;
                    }
                    for (int b = objArray.Length; b-- > 0; )
                    {
                        Object objItem = objArray.GetValue(b);
                        Object cloneItem = cloneArray.GetValue(b);
                        if (!EqualsObjects(objItem, cloneItem))
                        {
                            return false;
                        }
                    }
                    return true;
                }
                else if (objValue is IList && cloneValue is IList)
                {
                    IList objList = (IList)objValue;
                    IList cloneList = (IList)cloneValue;
                    if (objList.Count != cloneList.Count)
                    {
                        return false;
                    }
                    for (int b = objList.Count; b-- > 0; )
                    {
                        Object objItem = objList[b];
                        Object cloneItem = cloneList[b];
                        if (!EqualsObjects(objItem, cloneItem))
                        {
                            return false;
                        }
                    }
                    return true;
                }
                else if (TypeInfoProvider.GetTypeInfo(objValue.GetType()).DoesImplement(typeof(ISet<>))
                        && TypeInfoProvider.GetTypeInfo(cloneValue.GetType()).DoesImplement(typeof(ISet<>)))
                {
                    if (((ICollection)objValue).Count != ((ICollection)cloneValue).Count)
                    {
                        return false;
                    }
                    MethodInfo setEqualsMethod = cloneValue.GetType().GetMethod("SetEquals");
                    return (bool)setEqualsMethod.Invoke(cloneValue, new Object[] { objValue });
                }
                else if (objValue is ICollection && cloneValue is ICollection)
                {
                    IEnumerator objIter = ((IEnumerable)objValue).GetEnumerator();
                    IEnumerator cloneIter = ((IEnumerable)cloneValue).GetEnumerator();
                    while (objIter.MoveNext())
                    {
                        if (!cloneIter.MoveNext())
                        {
                            return false;
                        }
                        Object objItem = objIter.Current;
                        Object cloneItem = cloneIter.Current;
                        if (!EqualsObjects(objItem, cloneItem))
                        {
                            return false;
                        }
                    }
                    if (cloneIter.MoveNext())
                    {
                        return false;
                    }
                    return true;
                }
                else if (objValue is IEnumerable && cloneValue is IEnumerable)
                {
                    IEnumerator objIter = ((IEnumerable)objValue).GetEnumerator();
                    IEnumerator cloneIter = ((IEnumerable)cloneValue).GetEnumerator();
                    while (objIter.MoveNext())
                    {
                        if (!cloneIter.MoveNext())
                        {
                            return false;
                        }
                        Object objItem = objIter.Current;
                        Object cloneItem = cloneIter.Current;
                        if (!EqualsObjects(objItem, cloneItem))
                        {
                            return false;
                        }
                    }
                    if (cloneIter.MoveNext())
                    {
                        return false;
                    }
                    return true;
                }
            }
            return EqualsObjects(objValue, cloneValue);
        }

        protected virtual bool EqualsObjects(Object left, Object right)
        {
            if (left == null)
            {
                return (right == null);
            }
            if (right == null)
            {
                return false;
            }
            if (left.Equals(right))
            {
                return true;
            }
            //foreach (IMergeExtension mergeExtension in mergeExtensions)
            //{
            //    if (mergeExtension.HandlesType(left.GetType()))
            //    {
            //        return mergeExtension.EqualsObjects(left, right);
            //    }
            //}
            return false;
        }

        protected virtual IList<IUpdateItem> AddModification(Object obj, MergeHandle handle)
        {
            IList<IUpdateItem> modItemList = handle.objToModDict.Get(obj);
            if (modItemList == null)
            {
                modItemList = new List<IUpdateItem>();
                handle.objToModDict.Put(obj, modItemList);
            }
            return modItemList;
        }

        protected virtual void AddModification(Object obj, String memberName, Type targetValueType, Object value, Object cloneValue, MergeHandle handle)
        {
            //foreach (IMergeExtension mergeExtension in mergeExtensions)
            //{
            //    if (mergeExtension.HandlesType(targetValueType))
            //    {
            //        value = mergeExtension.ExtractPrimitiveValueToMerge(value);
            //    }
            //}
            PrimitiveUpdateItem primModItem = new PrimitiveUpdateItem();
            primModItem.MemberName = memberName;
            primModItem.NewValue = value;

            IList<IUpdateItem> modItemList = AddModification(obj, handle);
            modItemList.Add(primModItem);
        }

        protected virtual void AddOriModification(Object obj, String memberName, Object value, Object cloneValue, MergeHandle handle)
        {
            if (value is IList)
            {
                IList list = (IList)value;
                for (int a = 0, size = list.Count; a < size; a++)
                {
                    Object objItem = list[a];
                    MergeOrPersist(objItem, handle);
                }
            }
            else if (value is IEnumerable)
            {
                IEnumerator objEnumerator = ((IEnumerable)value).GetEnumerator();
                while (objEnumerator.MoveNext())
                {
                    Object objItem = objEnumerator.Current;
                    MergeOrPersist(objItem, handle);
                }
            }
            else
            {
                MergeOrPersist(value, handle);
            }
            try
            {
                IList<IObjRef> oldOriList = OriHelper.ExtractObjRefList(cloneValue, handle, handle.oldOriList);
                IList<IObjRef> newOriList = OriHelper.ExtractObjRefList(value, handle, handle.newOriList);

                IRelationUpdateItem oriModItem = CreateRUI(memberName, oldOriList, newOriList);
                if (oriModItem == null)
                {
                    return;
                }
                IList<IUpdateItem> modItemList = AddModification(obj, handle);

                modItemList.Add(oriModItem);
            }
            finally
            {
                handle.oldOriList.Clear();
                handle.newOriList.Clear();
            }
        }

        public IRelationUpdateItem CreateRUI(String memberName, IList<IObjRef> oldOriList, IList<IObjRef> newOriList)
	    {
		    if (oldOriList.Count == 0 && newOriList.Count == 0)
		    {
			    return null;
		    }
		    IISet<IObjRef> oldSet = oldOriList.Count > 0 ? new CHashSet<IObjRef>(oldOriList) : EmptySet.Empty<IObjRef>();
		    IISet<IObjRef> newSet = newOriList.Count > 0 ? new CHashSet<IObjRef>(newOriList) : EmptySet.Empty<IObjRef>();

		    IISet<IObjRef> smallerSet = ((ICollection)oldSet).Count > ((ICollection)newSet).Count ? newSet : oldSet;
		    IISet<IObjRef> greaterSet = ((ICollection)oldSet).Count > ((ICollection)newSet).Count ? oldSet : newSet;

		    // Check unchanged ORIs
		    Iterator<IObjRef> smallerIter = smallerSet.Iterator();
		    while (smallerIter.MoveNext())
		    {
			    // Old ORIs, which exist as new ORIs, too, are unchanged
			    IObjRef objRef = smallerIter.Current;
			    if (greaterSet.Remove(objRef))
			    {
				    smallerIter.Remove();
			    }
		    }
		    if (((ICollection)oldSet).Count == 0 && ((ICollection)newSet).Count == 0)
		    {
			    return null;
		    }
		    // Old ORIs are now handled as REMOVE, New ORIs as ADD
		    RelationUpdateItem rui = new RelationUpdateItem();
		    rui.MemberName = memberName;
		    if (((ICollection)oldSet).Count > 0)
		    {
			    rui.RemovedORIs = oldSet.ToArray();
		    }
		    if (((ICollection)newSet).Count > 0)
		    {
			    rui.AddedORIs = newSet.ToArray();
		    }
		    return rui;
	    }

	    public RelationUpdateItemBuild CreateRUIBuild(String memberName, IList<IObjRef> oldOriList, IList<IObjRef> newOriList)
	    {
		    if (oldOriList.Count == 0 && newOriList.Count == 0)
		    {
			    return null;
		    }
            IISet<IObjRef> oldSet = oldOriList.Count > 0 ? new CHashSet<IObjRef>(oldOriList) : EmptySet.Empty<IObjRef>();
            IISet<IObjRef> newSet = newOriList.Count > 0 ? new CHashSet<IObjRef>(newOriList) : EmptySet.Empty<IObjRef>();

            IISet<IObjRef> smallerSet = ((ICollection)oldSet).Count > ((ICollection)newSet).Count ? newSet : oldSet;
            IISet<IObjRef> greaterSet = ((ICollection)oldSet).Count > ((ICollection)newSet).Count ? oldSet : newSet;

            // Check unchanged ORIs
            Iterator<IObjRef> smallerIter = smallerSet.Iterator();
            while (smallerIter.MoveNext())
            {
                // Old ORIs, which exist as new ORIs, too, are unchanged
                IObjRef objRef = smallerIter.Current;
                if (greaterSet.Remove(objRef))
                {
                    smallerIter.Remove();
                }
            }
            if (((ICollection)oldSet).Count == 0 && ((ICollection)newSet).Count == 0)
            {
                return null;
            }
		    // Old ORIs are now handled as REMOVE, New ORIs as ADD
		    RelationUpdateItemBuild rui = new RelationUpdateItemBuild(memberName);
            if (((ICollection)oldSet).Count > 0)
            {
                rui.RemoveObjRefs(oldSet);
            }
            if (((ICollection)newSet).Count > 0)
            {
                rui.AddObjRefs(newSet);
            }
		    return rui;
	    }

        protected virtual bool IsMemberModified(Object objValue, Object cloneValue, MergeHandle handle, IEntityMetaData metaData)
        {
            if (objValue == null)
            {
                return (cloneValue != null);
            }
            if (cloneValue == null)
            {
                MergeDeepIntern(objValue, handle);
                return true;
            }
            if (objValue is IList)
            {
                IList objList = (IList)objValue;
                IList cloneList = (IList)cloneValue;

                bool memberModified = false;

                if (objList.Count != cloneList.Count)
                {
                    memberModified = true;
                }
                for (int a = 0, size = objList.Count; a < size; a++)
                {
                    Object objItem = objList[a];

                    if (cloneList.Count > a)
                    {
                        Object cloneItem = cloneList[a];

                        if (!EqualsReferenceOrId(objItem, cloneItem, handle, metaData))
                        {
                            memberModified = true;
                        }
                    }
                    MergeOrPersist(objItem, handle);
                }
                return memberModified;
            }
            if (objValue is IEnumerable)
            {
                IEnumerator objEnumerator = ((IEnumerable)objValue).GetEnumerator();
                IEnumerator cloneEnumerator = ((IEnumerable)cloneValue).GetEnumerator();

                bool memberModified = false;
                while (objEnumerator.MoveNext())
                {
                    if (!cloneEnumerator.MoveNext())
                    {
                        memberModified = true;
                    }
                    else
                    {
                        if (!EqualsReferenceOrId(objEnumerator.Current, cloneEnumerator.Current, handle, metaData))
                        {
                            memberModified = true;
                        }
                    }
                    MergeOrPersist(objEnumerator.Current, handle);
                }
                return memberModified;
            }
            MergeOrPersist(objValue, handle);
            return !EqualsReferenceOrId(objValue, cloneValue, handle, metaData);
        }

        protected virtual bool EqualsReferenceOrId(Object original, Object clone, MergeHandle handle, IEntityMetaData metaData)
        {
            if (original == null)
            {
                return (clone == null);
            }
            if (clone == null)
            {
                return false;
            }
            PrimitiveMember keyMember = metaData.IdMember;
            return Object.Equals(keyMember.GetValue(clone, true), keyMember.GetValue(original, true));
        }
    }
}
