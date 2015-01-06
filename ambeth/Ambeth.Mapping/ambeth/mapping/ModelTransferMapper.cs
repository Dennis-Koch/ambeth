using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Filter.Model;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Mapping.Config;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;

namespace De.Osthus.Ambeth.Mapping
{
    public class ModelTransferMapper : IMapperService, IDisposable
    {
        protected static readonly Object NOT_YET_READY = new Object();

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ICacheHelper CacheHelper { protected get; set; }

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired]
        public ICache Cache { protected get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IListTypeHelper ListTypeHelper { protected get; set; }

        [Autowired]
        public IDedicatedMapperRegistry MapperExtensionRegistry { protected get; set; }

        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }
        
        [Autowired]
        public IObjRefHelper OriHelper { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        [Autowired]
        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        protected readonly HashMap<Type, IMap<String, ITypeInfoItem>> typeToTypeInfoMap = new HashMap<Type, IMap<String, ITypeInfoItem>>();

        protected readonly HashMap<IObjRef, IObjRef> alreadyCreatedObjRefsMap = new HashMap<IObjRef, IObjRef>();

        protected readonly IdentityHashMap<Object, IMap<Type, Object>> boToSpecifiedVOMap = new IdentityHashMap<Object, IMap<Type, Object>>();

        protected readonly IdentityHashMap<Object, Object> voToBoMap = new IdentityHashMap<Object, Object>();

        protected readonly HashMap<CompositIdentityClassKey, Object> reverseRelationMap = new HashMap<CompositIdentityClassKey, Object>();

        protected readonly IdentityHashSet<Object> allBOsToKeepInCache = new IdentityHashSet<Object>();

        protected readonly IdentityHashSet<Object> bosToRemoveTempIdFrom = new IdentityHashSet<Object>();

        protected readonly IdentityHashSet<Object> vosToRemoveTempIdFrom = new IdentityHashSet<Object>();

        protected long nextTempId = -1;

        [Property(MappingConfigurationConstants.InitDirectRelationsInBusinessObjects, DefaultValue = "true")]
        public bool initDirectRelationsInBusinessObjects { protected get; set; }
        
        public void Dispose()
        {
            ConversionHelper = null;
            EntityMetaDataProvider = null;
            TypeInfoProvider = null;
            OriHelper = null;
            Cache = null;
        }

        public Object MapToBusinessObject(Object valueObject)
        {
            if (valueObject == null)
            {
                return null;
            }
            IList<Object> valueObjects = new Object[] { valueObject };
            IList<Object> results = MapToBusinessObjectList(valueObjects);
            return results[0];
        }

        public IList<Object> MapToBusinessObjectListFromListType(Object listTypeObject)
        {
            IList<Object> valueObjectList = (IList<Object>)ListTypeHelper.UnpackListType(listTypeObject);
            return MapToBusinessObjectList(valueObjectList);
        }

        public IList<Object> MapToBusinessObjectList(IList<Object> valueObjectList)
        {
            if (valueObjectList.Count == 0)
            {
                return EmptyList.Empty<Object>();
            }
            ICacheIntern cache = (ICacheIntern) this.Cache.CurrentCache;
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            IdentityHashMap<Object, Object> voToBoMap = this.voToBoMap;
            List<Object> allValueObjects = new List<Object>(valueObjectList.Count);
            bool acquiredHardRefs = cache.AcquireHardRefTLIfNotAlready();
            bool oldActive = CacheModification.Active;
            CacheModification.Active = true;
            try
            {
                ResolveAllValueObjectsDirectly(valueObjectList, allValueObjects, IdentityHashSet<Object>.Create(valueObjectList.Count), null);

                MapBosByVos(allValueObjects, cache);

                for (int i = allValueObjects.Count; i-- > 0; )
                {
                    ResolvePrimitiveProperties(allValueObjects[i], cache);
                }

                List<DirectValueHolderRef> boToPendingRelationsList = new List<DirectValueHolderRef>();
                CHashSet<IObjRef> referencedBOsSet = new CHashSet<IObjRef>();
                HashMap<IObjRef, IObjRef> alreadyCreatedObjRefMap = new HashMap<IObjRef, IObjRef>();
                try
                {
                    for (int i = allValueObjects.Count; i-- > 0; )
                    {
                        CollectReferencedBusinessObjects(allValueObjects[i], referencedBOsSet, boToPendingRelationsList, alreadyCreatedObjRefMap, cache);
                    }
                    IList<IObjRef> referencedBOsList = referencedBOsSet.ToList();

                    if (initDirectRelationsInBusinessObjects)
                    {
                        IPrefetchState prefetchState = PrefetchHelper.Prefetch(boToPendingRelationsList);
                        // Store retrieved BOs to hard ref to suppress Weak GC handling of cache
                        allBOsToKeepInCache.Add(prefetchState);

                        IList<Object> referencedBOs = cache.GetObjects(referencedBOsList, CacheDirective.FailEarly | CacheDirective.ReturnMisses);

                        for (int a = referencedBOs.Count; a-- > 0; )
                        {
                            Object referencedBO = referencedBOs[a];
                            if (referencedBO == null)
                            {
                                throw new MappingException("At least one entity could not be found: " + referencedBOsList[a].ToString());
                            }
                        }
                        //// Allocate specific pending relations to their bo fields
                        //for (int a = boToPendingRelationsList.Count; a-- > 0;)
                        //{
                        //    PendingRelation pendingRelation = boToPendingRelationsList[a];
                        //    Object businessObject = pendingRelation.BusinessObject;
                        //    IRelationInfoItem member = pendingRelation.Member;
                        //    IList<IObjRef> pendingObjRefs = pendingRelation.PendingObjRefs;

                        //    // Everything which gets missed by now does not exist in the DB.
                        //    // FailEarly is important to suppress redundant tries of previously failed loadings
                        //    IList<Object> pendingObjects = childCache.GetObjects(pendingObjRefs, CacheDirective.failEarly());

                        //    Object convertedPendingObjects = ConvertPrimitiveValue(pendingObjects, member.ElementType, member);
                        //    member.SetValue(businessObject, convertedPendingObjects);
                        //}
                    }
                }
                finally
                {
                    alreadyCreatedObjRefMap = null;
                }

                List<Object> allBusinessObjects = new List<Object>(allValueObjects.Count);

                List<DirectValueHolderRef> objRefContainers = new List<DirectValueHolderRef>(allValueObjects.Count);
                for (int i = allValueObjects.Count; i-- > 0; )
                {
                    Object valueObject = allValueObjects[i];
                    Object businessObject = voToBoMap.Get(valueObject);

                    IDedicatedMapper dedicatedMapper = MapperExtensionRegistry.GetDedicatedMapper(businessObject.GetType());
                    if (dedicatedMapper != null)
                    {
                        dedicatedMapper.ApplySpecialMapping(businessObject, valueObject, CopyDirection.VO_TO_BO);
                    }

                    allBusinessObjects.Add(businessObject);
                    if (!initDirectRelationsInBusinessObjects)
                    {
                        continue;
                    }
                    IEntityMetaData metaData = ((IEntityMetaDataHolder)businessObject).Get__EntityMetaData();
                    RelationMember[] relationMembers = metaData.RelationMembers;
                    if (relationMembers.Length == 0)
                    {
                        continue;
                    }
                    IValueHolderContainer vhc = (IValueHolderContainer)businessObject;
                    for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
                    {
                        RelationMember relationMember = relationMembers[relationIndex];
                        if (ValueHolderState.INIT == vhc.Get__State(relationIndex))
                        {
                            continue;
                        }
                        objRefContainers.Add(new DirectValueHolderRef(vhc, relationMember));
                    }
                }
                if (objRefContainers.Count > 0)
                {
                    PrefetchHelper.Prefetch(objRefContainers);
                }
                List<IObjRef> orisToGet = new List<IObjRef>(valueObjectList.Count);

                for (int i = 0, size = valueObjectList.Count; i < size; i++)
                {
                    Object rootValueObject = valueObjectList[i];
                    IValueObjectConfig config = GetValueObjectConfig(rootValueObject.GetType());
                    IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(config.EntityType);
                    IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);
                    Object id = GetIdFromValueObject(rootValueObject, metaData, boNameToVoMember, config);

                    ObjRef objRef = new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null);
                    orisToGet.Add(objRef);
                }
                IList<Object> businessObjectList = cache.GetObjects(orisToGet, CacheDirective.FailEarly | CacheDirective.ReturnMisses);
                ClearObjectsWithTempIds((IWritableCache)cache);

                for (int a = allBusinessObjects.Count; a-- > 0;)
			    {
				    Object businessObject = allBusinessObjects[a];
				    if (businessObject is IDataObject)
				    {
					    ((IDataObject) businessObject).ToBeUpdated = true;
				    }
			    }
                return businessObjectList;
            }
            finally
            {
                CacheModification.Active = oldActive;
                cache.ClearHardRefs(acquiredHardRefs);
            }
        }

        public Object MapToValueObject(Object businessObject, Type valueObjectType)
        {
            if (businessObject == null)
            {
                return null;
            }
            IList<Object> businessObjects = new Object[] { businessObject };
            IList<Object> results = MapToValueObjectList(businessObjects, valueObjectType);
            return results[0];
        }

        public Object MapToValueObjectListType(IList<Object> businessObjectList, Type valueObjectType, Type listType)
        {
            IList<Object> valueObjectList = MapToValueObjectList(businessObjectList, valueObjectType);
            return ListTypeHelper.PackInListType(valueObjectList, listType);
        }

        public Object MapToValueObjectRefListType(IList<Object> businessObjectList, Type valueObjectRefListType)
        {
            IList<Object> valueObjectList = MapToValueObjectRefList(businessObjectList);
            return ListTypeHelper.PackInListType(valueObjectList, valueObjectRefListType);
        }

        public IList<Object> MapToValueObjectList(IList<Object> businessObjectList, Type valueObjectType)
        {
            if (businessObjectList.Count == 0)
            {
                return EmptyList.Empty<Object>();
            }
            ICache cache = this.Cache.CurrentCache;
            IPrefetchHelper prefetchHelper = this.PrefetchHelper;
            // Ensure all potential value-holders of To-One BOs are initialized in a batch
            prefetchHelper.Prefetch(businessObjectList);
            // Checking for correct types
            IEntityMetaData boMetaData = ((IEntityMetaDataHolder)businessObjectList[0]).Get__EntityMetaData();
            Type businessObjectType = boMetaData.EntityType;
            IValueObjectConfig config = GetValueObjectConfig(valueObjectType);
            if (!config.EntityType.Equals(businessObjectType))
            {
                throw new ArgumentException("'" + businessObjectType.FullName + "' cannot be mapped to '" + valueObjectType.FullName + "'");
            }

            List<Object> pendingValueHolders = new List<Object>();
            List<IBackgroundWorkerDelegate> runnables = new List<IBackgroundWorkerDelegate>();
            List<Object> valueObjectList = new List<Object>(businessObjectList.Count);
            for (int i = 0; i < businessObjectList.Count; i++)
            {
                Object businessObject = businessObjectList[i];
                Object valueObject = SubMapToCachedValueObject(businessObject, valueObjectType, pendingValueHolders, runnables);

                valueObjectList.Add(valueObject);
            }
            while (pendingValueHolders.Count > 0 || runnables.Count > 0)
            {
                if (pendingValueHolders.Count > 0)
                {
                    prefetchHelper.Prefetch(pendingValueHolders);
                    pendingValueHolders.Clear();
                }
                List<IBackgroundWorkerDelegate> runnablesClone = new List<IBackgroundWorkerDelegate>(runnables);

                // Reset ORIGINAL lists because they may have been referenced from within cascading runnables
                runnables.Clear();

                for (int a = 0, size = runnablesClone.Count; a < size; a++)
                {
                    runnablesClone[a]();
                }
                // PendingValueHolders might be (re-)filled after the runnables. So we need a while loop
            }

            ClearObjectsWithTempIds((IWritableCache)cache);

            return valueObjectList;
        }

        protected IList<Object> MapToValueObjectRefList(IList<Object> businessObjectList)
        {
            if (businessObjectList.Count == 0)
            {
                return EmptyList.Empty<Object>();
            }
            // Checking for correct types
            List<Object> refList = new List<Object>(businessObjectList.Count);

            for (int a = 0, size = businessObjectList.Count; a < size; a++)
            {
                Object businessObject = businessObjectList[a];
                IEntityMetaData metaData = ((IEntityMetaDataHolder)businessObject).Get__EntityMetaData();

                PrimitiveMember idMember = SelectIdMember(metaData);
                Object id = idMember.GetValue(businessObject, false);
                if (id == null)
                {
                    throw new ArgumentException("BusinessObject '" + businessObject + "' at index " + a + " does not have a valid ID");
                }
                refList.Add(id);
            }
            return refList;
        }

        protected void ResolveProperties(Object businessObject, Object valueObject, ICollection<Object> pendingValueHolders,
                ICollection<IBackgroundWorkerDelegate> runnables)
        {
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            IEntityMetaData businessObjectMetaData = ((IEntityMetaDataHolder)businessObject).Get__EntityMetaData();
            IValueObjectConfig config = entityMetaDataProvider.GetValueObjectConfig(valueObject.GetType());
            IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);

            CopyPrimitives(businessObject, valueObject, config, CopyDirection.BO_TO_VO, businessObjectMetaData, boNameToVoMember);

            RelationMember[] relationMembers = businessObjectMetaData.RelationMembers;
		    if (relationMembers.Length == 0)
		    {
			    return;
		    }
		    IObjRefContainer vhc = (IObjRefContainer) businessObject;

            for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
            {
                RelationMember boMember = relationMembers[relationIndex];
                String boMemberName = boMember.Name;
                String voMemberName = config.GetValueObjectMemberName(boMemberName);
                ITypeInfoItem voMember = boNameToVoMember.Get(boMemberName);
                if (config.IsIgnoredMember(voMemberName) || voMember == null)
                {
                    continue;
                }
                Object voMemberValue = CreateVOMemberValue(vhc, relationIndex, boMember, config, voMember, pendingValueHolders, runnables);
                if (!Object.ReferenceEquals(voMemberValue, NOT_YET_READY))
                {
                    voMember.SetValue(valueObject, voMemberValue);
                }
                else
                {
                    runnables.Add(delegate()
                    {
                        Object voMemberValue2 = CreateVOMemberValue(vhc, relationIndex, boMember, config, voMember, pendingValueHolders, runnables);
                        if (Object.ReferenceEquals(voMemberValue2, NOT_YET_READY))
                        {
                            throw new Exception("Must never happen");
                        }
                        voMember.SetValue(valueObject, voMemberValue2);
                    });
                }
            }
        }

        protected void MapBosByVos(IList<Object> valueObjects, ICacheIntern cache)
        {
            List<IObjRef> toLoad = new List<IObjRef>();
            List<Object> waitingVOs = new List<Object>();
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            IMap<Object, Object> voToBoMap = this.voToBoMap;
            for (int i = valueObjects.Count; i-- > 0; )
            {
                Object valueObject = valueObjects[i];
                if (valueObject == null || voToBoMap.ContainsKey(valueObject))
                {
                    continue;
                }
                IValueObjectConfig config = GetValueObjectConfig(valueObject.GetType());
                IEntityMetaData boMetaData = entityMetaDataProvider.GetMetaData(config.EntityType);
                IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);

                Object businessObject = null;
                Object id = GetIdFromValueObject(valueObject, boMetaData, boNameToVoMember, config);
                if (id != null)
                {
                    if (initDirectRelationsInBusinessObjects)
                    {
                        IObjRef ori = GetObjRef(config.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, alreadyCreatedObjRefsMap);
                        toLoad.Add(ori);
                        waitingVOs.Add(valueObject);
                    }
                    else
                    {
                        businessObject = CreateBusinessObject(boMetaData, cache);
                        voToBoMap.Put(valueObject, businessObject);
                    }
                }
                else
                {
                    businessObject = CreateBusinessObject(boMetaData, cache);
                    SetTempIdToValueObject(valueObject, boMetaData, boNameToVoMember, config);
                    bosToRemoveTempIdFrom.Add(businessObject);
                    id = GetIdFromValueObject(valueObject, boMetaData, boNameToVoMember, config);
                    voToBoMap.Put(valueObject, businessObject);
                }
            }

            if (toLoad.Count > 0)
            {
                IList<Object> businessObjects = cache.GetObjects(toLoad, CacheDirective.ReturnMisses);
                for (int i = businessObjects.Count; i-- > 0; )
                {
                    Object businessObject = businessObjects[i];
                    Object valueObject = waitingVOs[i];
                    if (businessObject == null)
                    {
                        IValueObjectConfig config = GetValueObjectConfig(valueObject.GetType());
                        IEntityMetaData boMetaData = entityMetaDataProvider.GetMetaData(config.EntityType);
                        businessObject = CreateBusinessObject(boMetaData, cache);
                    }
                    voToBoMap.Put(valueObject, businessObject);
                }
            }
        }

        protected Object CreateBusinessObject(IEntityMetaData boMetaData, ICacheIntern cache)
	    {
		    Object businessObject = EntityFactory.CreateEntity(boMetaData);
		    if (businessObject is IValueHolderContainer)
		    {
			    ((IValueHolderContainer) businessObject).__TargetCache = cache;
		    }
		    return businessObject;
	    }

        protected void ResolvePrimitiveProperties(Object valueObject, ICacheIntern cache)
        {
            IValueObjectConfig config = GetValueObjectConfig(valueObject.GetType());

            IEntityMetaData boMetaData = EntityMetaDataProvider.GetMetaData(config.EntityType);
            IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);

            Object businessObject = voToBoMap.Get(valueObject);
            if (businessObject == null)
            {
                throw new Exception("Must never happen");
            }

            Object[] primitives = CopyPrimitives(businessObject, valueObject, config, CopyDirection.VO_TO_BO, boMetaData, boNameToVoMember);

            Object id = boMetaData.IdMember.GetValue(businessObject, false);
            Object version = boMetaData.VersionMember.GetValue(businessObject, false);
            cache.AddDirect(boMetaData, id, version, businessObject, primitives, null);// relationValues);
        }

        protected void CollectReferencedBusinessObjects(Object valueObject, IISet<IObjRef> referencedBOsSet, IList<DirectValueHolderRef> boToPendingRelationsList,
                IMap<IObjRef, IObjRef> alreadyCreatedObjRefMap, ICacheIntern cache)
        {
            IValueObjectConfig config = GetValueObjectConfig(valueObject.GetType());

            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            IEntityMetaData boMetaData = entityMetaDataProvider.GetMetaData(config.EntityType);
            IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);

            IdentityHashMap<Object, Object> voToBoMap = this.voToBoMap;

            RelationMember[] relationMembers = boMetaData.RelationMembers;
            if (relationMembers.Length == 0)
            {
                return;
            }
            IValueHolderContainer businessObject = (IValueHolderContainer) voToBoMap.Get(valueObject);
            if (businessObject == null)
            {
                throw new Exception("Must never happen");
            }
            ICacheHelper cacheHelper = this.CacheHelper;
            IConversionHelper conversionHelper = this.ConversionHelper;
            IListTypeHelper listTypeHelper = this.ListTypeHelper;
            HashMap<CompositIdentityClassKey, Object> reverseRelationMap = this.reverseRelationMap;

            for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
            {
                RelationMember boMember = relationMembers[relationIndex];
                String boMemberName = boMember.Name;
                String voMemberName = config.GetValueObjectMemberName(boMemberName);

                ITypeInfoItem voMember = boNameToVoMember.Get(boMemberName);
                Object voValue = null;
                if (voMember != null)
                {
                    if (config.IsIgnoredMember(voMemberName))
                    {
                        // Nothing to collect
                        Object convertedEmptyRelation = ConvertPrimitiveValue(EmptyList.Empty<Object>(), boMember.ElementType, boMember);
                        boMember.SetValue(businessObject, convertedEmptyRelation);
                        continue;
                    }
                    voValue = voMember.GetValue(valueObject);
                }
                else
                {
                    Object boValue = null;
                    // Workaround bis das Problem (TODO) behoben ist, um zumindest eindeutige Relationen fehlerfrei
                    // aufzuloesen.
                    CompositIdentityClassKey key = new CompositIdentityClassKey(valueObject, boMember.ElementType);
                    voValue = reverseRelationMap.Get(key);
                    if (voValue != null)
                    {
                        boValue = voToBoMap.Get(voValue);
                        boMember.SetValue(businessObject, boValue);
                        continue;
                    }
                    Object id = boMetaData.IdMember.GetValue(businessObject, false);
                    if (id != null)
                    {
                        // TODO value ueber die Rueckreferenz finden
                        // Bis dahin wird es nach dem Mapping beim Speichern knallen, weil der LazyValueHolder bei neuen
                        // Entitaeten nicht aufgeloest werden kann.
                        if (ValueHolderState.INIT != businessObject.Get__State(relationIndex))
                        {
                            businessObject.Set__Uninitialized(relationIndex, null);
                        }
                    }
                    else if (boMember.RealType.Equals(boMember.ElementType))
                    {
                        // To-one relation
                        boValue = null;
                        boMember.SetValue(businessObject, boValue);
                    }
                    else
                    {
                        // To-many relation
                        boValue = ListUtil.CreateCollectionOfType(boMember.RealType, 0);
                        boMember.SetValue(businessObject, boValue);
                    }
                    continue;
                }
                if (voValue == null)
                {
                    // Nothing to collect
                    Object convertedEmptyRelation = ConvertPrimitiveValue(EmptyList.Empty<Object>(), boMember.ElementType, boMember);
                    boMember.SetValue(businessObject, convertedEmptyRelation);
                    continue;
                }
                if (config.HoldsListType(voMember.Name))
                {
                    voValue = listTypeHelper.UnpackListType(voValue);
                }
                IList<Object> voList = ListUtil.AnyToList(voValue);
                if (voList.Count == 0)
                {
                    // Nothing to collect
                    Object convertedEmptyRelation = ConvertPrimitiveValue(EmptyList.Empty<Object>(), boMember.ElementType, boMember);
                    boMember.SetValue(businessObject, convertedEmptyRelation);
                    continue;
                }
                IEntityMetaData boMetaDataOfItem = entityMetaDataProvider.GetMetaData(boMember.ElementType);
                PrimitiveMember boIdMemberOfItem = SelectIdMember(boMetaDataOfItem);
                sbyte idIndex = boMetaDataOfItem.GetIdIndexByMemberName(boIdMemberOfItem.Name);

                List<IObjRef> pendingRelations = new List<IObjRef>();

                ValueObjectMemberType memberType = config.GetValueObjectMemberType(voMemberName);
                bool mapAsBasic = memberType == ValueObjectMemberType.BASIC;

                if (!mapAsBasic)
                {
                    for (int a = 0, size = voList.Count; a < size; a++)
                    {
                        Object voItem = voList[a];

                        IValueObjectConfig configOfItem = entityMetaDataProvider.GetValueObjectConfig(voItem.GetType());

                        if (configOfItem == null)
                        {
                            // This is a simple id which we can use
                            IObjRef objRef = GetObjRef(boMetaDataOfItem.EntityType, idIndex, voItem, alreadyCreatedObjRefsMap);
                            referencedBOsSet.Add(objRef);
                            pendingRelations.Add(objRef);
                            continue;
                        }
                        // voItem is a real VO handle
                        Object boItem = voToBoMap.Get(voItem);
                        Object idOfItem = GetIdFromBusinessObject(boItem, boMetaDataOfItem);
                        if (idOfItem == null)
                        {
                            throw new Exception("All BOs must have at least a temporary id at this point. " + boItem);
                        }
                        IObjRef objRef2 = GetObjRef(boMetaDataOfItem.EntityType, ObjRef.PRIMARY_KEY_INDEX, idOfItem, alreadyCreatedObjRefsMap);
                        referencedBOsSet.Add(objRef2);
                        pendingRelations.Add(objRef2);
                    }
                }

                if (mapAsBasic)
                {
                    Type targetType = boMember.ElementType;
                    List<Object> boList = new List<Object>();
                    for (int a = 0, size = voList.Count; a < size; a++)
                    {
                        Object voItem = voList[a];
                        Object boItem = conversionHelper.ConvertValueToType(targetType, voItem);
                        boList.Add(boItem);
                    }
                    Object relationValue = cacheHelper.ConvertResultListToExpectedType(boList, boMember.RealType, boMember.ElementType);
                    boMember.SetValue(businessObject, relationValue);
                }
                else if (pendingRelations.Count == 0)
                {
                    Object relationValue = cacheHelper.CreateInstanceOfTargetExpectedType(boMember.RealType, boMember.ElementType);
                    boMember.SetValue(businessObject, relationValue);
                }
                else
                {
                    IObjRef[] objRefs = pendingRelations.Count > 0 ? pendingRelations.ToArray() : ObjRef.EMPTY_ARRAY;
                    businessObject.Set__Uninitialized(relationIndex, objRefs);
				    businessObject.__TargetCache = cache;
                    referencedBOsSet.AddAll(objRefs);
                    boToPendingRelationsList.Add(new DirectValueHolderRef(businessObject, boMember));
                }
            }
        }

        protected IObjRef GetObjRef(Type entityType, sbyte idIndex, Object id, IMap<IObjRef, IObjRef> alreadyCreatedObjRefMap)
        {
            ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
            IObjRef usingObjRef = alreadyCreatedObjRefMap.Get(objRef);
            if (usingObjRef == null)
            {
                alreadyCreatedObjRefMap.Put(objRef, objRef);
                usingObjRef = objRef;
            }
            return usingObjRef;
        }

        protected Object CreateVOMemberValue(IObjRefContainer businessObject, int relationIndex, RelationMember boMember, IValueObjectConfig config, ITypeInfoItem voMember,
                ICollection<Object> pendingValueHolders, ICollection<IBackgroundWorkerDelegate> runnables)
        {
            Object voMemberValue = null;
            Type voMemberType = voMember.RealType;
            bool holdsListType = config.HoldsListType(voMember.Name);
            bool singularValue = voMemberType.Equals(voMember.ElementType) && !holdsListType;

            // TODO: How to check for instance of IList? what if it is IList<T> ?
            //if (!singularValue && !List.class.isAssignableFrom(voMemberType) && !holdsListType)
            //{
            //    throw new ArgumentException("Unsupportet collection type '" + voMemberType.getName() + "'");
            //}
            if (ValueHolderState.INIT != businessObject.Get__State(relationIndex))
            {
                pendingValueHolders.Add(new DirectValueHolderRef(businessObject, boMember));
                return NOT_YET_READY;
            }
            Object boValue = boMember.GetValue(businessObject, false);

            IList<Object> referencedBOs = ListUtil.AnyToList(boValue);
            IList<Object> referencedVOs = null;
            IConversionHelper conversionHelper = this.ConversionHelper;
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;

            if (referencedBOs.Count > 0)
            {
                referencedVOs = new List<Object>(referencedBOs.Count);

                Type voMemberElementType = voMember.ElementType;
                IValueObjectConfig refConfig = entityMetaDataProvider.GetValueObjectConfig(voMemberElementType);
                bool mapAsBasic = config.GetValueObjectMemberType(voMember.Name) == ValueObjectMemberType.BASIC;
                IEntityMetaData referencedBOMetaData = entityMetaDataProvider.GetMetaData(boMember.ElementType);
                PrimitiveMember refBOBuidMember = SelectIdMember(referencedBOMetaData);
                PrimitiveMember refBOVersionMember = referencedBOMetaData.VersionMember;
                sbyte refBOBuidIndex = referencedBOMetaData.GetIdIndexByMemberName(refBOBuidMember.Name);
                Type expectedVOType = config.GetMemberType(voMember.Name);

                IObjRefProvider buidOriProvider = new MappingObjRefProvider(refBOBuidMember, refBOVersionMember, refBOBuidIndex);

                for (int i = 0; i < referencedBOs.Count; i++)
                {
                    Object refBO = referencedBOs[i];
                    if (mapAsBasic)
                    {
                        Object refVO = conversionHelper.ConvertValueToType(expectedVOType, refBO);
                        referencedVOs.Add(refVO);
                        continue;
                    }
                    if (refConfig == null)
                    {
                        IObjRef refOri = OriHelper.GetCreateObjRef(refBO, buidOriProvider);
                        if (refOri == null || refOri.IdNameIndex != refBOBuidIndex)
                        {
                            throw new ArgumentException("ORI of referenced BO is null or does not contain BUID: " + refOri);
                        }

                        if (refOri.Id != null)
                        {
                            referencedVOs.Add(refOri.Id);
                        }
                        else
                        {
                            throw new Exception("Relation ID is null:" + refBO);
                        }
                    }
                    else
                    {
                        referencedVOs.Add(SubMapToCachedValueObject(refBO, voMemberElementType, pendingValueHolders, runnables));
                    }
                }
            }

            if (!singularValue)
            {
                if (holdsListType)
                {
                    voMemberValue = ListTypeHelper.PackInListType(referencedVOs, voMemberType);
                }
                else
                {
                    if (referencedVOs == null || voMemberType.IsAssignableFrom(referencedVOs.GetType()))
                    {
                        voMemberValue = referencedVOs;
                    }
                    else if (voMemberType.IsArray)
                    {
                        voMemberValue = ListUtil.AnyToArray(referencedVOs, voMemberType.GetElementType());
                    }
                }
            }
            else if (referencedVOs != null)
            {
                voMemberValue = referencedVOs[0];
            }

            return voMemberValue;
        }

        protected IValueObjectConfig GetValueObjectConfig(Type valueObjectType)
        {
            IValueObjectConfig config = EntityMetaDataProvider.GetValueObjectConfig(valueObjectType);
            if (config == null)
            {
                throw new Exception("No config found for value object type '" + valueObjectType.Name + "'");
            }
            return config;
        }

        protected void ResolveAllValueObjectsDirectly(Object valueObject, IList<Object> allDirectVOs, IdentityHashSet<Object> alreadyScannedSet, Object parent)
        {
            if (valueObject == null || !alreadyScannedSet.Add(valueObject))
            {
                return;
            }
            if (valueObject is IEnumerable)
            {
                foreach (Object item in (IEnumerable)valueObject)
                {
                    ResolveAllValueObjectsDirectly(item, allDirectVOs, alreadyScannedSet, parent);
                }
                return;
            }
            // filling map for resolving relations without back-link
            // null for root or non-unique cases
            Type parentBoType = null;
            if (parent != null)
            {
                IValueObjectConfig parentConfig = EntityMetaDataProvider.GetValueObjectConfig(parent.GetType());
                parentBoType = parentConfig.EntityType;
            }
            CompositIdentityClassKey key = new CompositIdentityClassKey(valueObject, parentBoType);
            if (!reverseRelationMap.ContainsKey(key))
            {
                reverseRelationMap.Put(key, parent);
            }
            else
            {
                reverseRelationMap.Put(key, null);
            }

            IValueObjectConfig config = EntityMetaDataProvider.GetValueObjectConfig(valueObject.GetType());
            if (config == null)
            {
                return;
            }
            allDirectVOs.Add(valueObject);

            if (HandleNoEntities(valueObject, config))
            {
                return;
            }
            IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(config.EntityType);
            foreach (ITypeInfoItem boMember in metaData.RelationMembers)
            {
                String boMemberName = boMember.Name;
                String voMemberName = config.GetValueObjectMemberName(boMemberName);
                ValueObjectMemberType valueObjectMemberType = config.GetValueObjectMemberType(voMemberName);
                ITypeInfoItem voMember = boNameToVoMember.Get(boMemberName);
                if (voMember == null || config.IsIgnoredMember(voMemberName) || valueObjectMemberType == ValueObjectMemberType.BASIC)
                {
                    // ValueObjectMemberType.BASIC members of entityType VO are special case mappings via conversionHelper
                    continue;
                }
                Object item = voMember.GetValue(valueObject, false);
                if (item == null)
                {
                    // Nothing to resolve
                    continue;
                }
                if (config.HoldsListType(voMember.Name))
                {
                    item = ListTypeHelper.UnpackListType(item);
                }

                ResolveAllValueObjectsDirectly(item, allDirectVOs, alreadyScannedSet, valueObject);
            }
        }

        protected bool HandleNoEntities(Object valueObject, IValueObjectConfig config)
        {
            Type entityType = config.EntityType;
            if (typeof(IFilterDescriptor).IsAssignableFrom(entityType))
            {
                return true;
            }
            else if (typeof(ISortDescriptor).IsAssignableFrom(entityType))
            {
                return true;
            }
            return false;
        }

        protected Object SubMapToCachedValueObject(Object subBusinessObject, Type valueObjectType, ICollection<Object> pendingValueHolders,
                ICollection<IBackgroundWorkerDelegate> runnables)
        {
            IMap<Type, Object> boVOsMap = boToSpecifiedVOMap.Get(subBusinessObject);

            if (boVOsMap == null)
            {
                boVOsMap = new IdentityHashMap<Type, Object>();
                boToSpecifiedVOMap.Put(subBusinessObject, boVOsMap);
            }
            IEntityMetaData metaData = ((IEntityMetaDataHolder)subBusinessObject).Get__EntityMetaData();
            Object subValueObject = boVOsMap.Get(valueObjectType);
            if (subValueObject == null)
            {
                subValueObject = Activator.CreateInstance(valueObjectType);
                boVOsMap.Put(valueObjectType, subValueObject);

                Object id = GetIdFromBusinessObject(subBusinessObject, metaData);
                if (id == null)
                {
                    SetTempIdToBusinessObject(subBusinessObject, metaData);
                    vosToRemoveTempIdFrom.Add(subValueObject);
                }
                ResolveProperties(subBusinessObject, subValueObject, pendingValueHolders, runnables);
            }

            IDedicatedMapper dedicatedMapper = MapperExtensionRegistry.GetDedicatedMapper(metaData.EntityType);
            if (dedicatedMapper != null)
            {
                dedicatedMapper.ApplySpecialMapping(subBusinessObject, subValueObject, CopyDirection.BO_TO_VO);
            }
            return subValueObject;
        }

        public Object GetIdFromValueObject(Object valueObject)
        {
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            IValueObjectConfig config = entityMetaDataProvider.GetValueObjectConfig(valueObject.GetType());
            IEntityMetaData boMetaData = entityMetaDataProvider.GetMetaData(config.EntityType);
            IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);
            return GetIdFromValueObject(valueObject, boMetaData, boNameToVoMember, config);
        }

        public Object GetVersionFromValueObject(Object valueObject)
        {
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            IValueObjectConfig config = entityMetaDataProvider.GetValueObjectConfig(valueObject.GetType());
            IEntityMetaData boMetaData = entityMetaDataProvider.GetMetaData(config.EntityType);
            IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);
            String boVersionMemberName = boMetaData.VersionMember.Name;
            ITypeInfoItem voVersionMember = boNameToVoMember.Get(boVersionMemberName);
            return voVersionMember.GetValue(valueObject, false);
        }

        protected Object GetIdFromValueObject(Object valueObject, IEntityMetaData boMetaData, IMap<String, ITypeInfoItem> boNameToVoMember, IValueObjectConfig config)
        {
            ITypeInfoItem voIdMember = getVoIdMember(config, boMetaData, boNameToVoMember);
            return voIdMember.GetValue(valueObject, false);
        }

        protected void SetTempIdToValueObject(Object valueObject, IEntityMetaData boMetaData, IMap<String, ITypeInfoItem> boNameToVoMember, IValueObjectConfig config)
        {
            ITypeInfoItem voIdMember = getVoIdMember(config, boMetaData, boNameToVoMember);
            Object tempId = GetNextTempIdAs(voIdMember.ElementType);
            voIdMember.SetValue(valueObject, tempId);
            vosToRemoveTempIdFrom.Add(valueObject);
        }

        protected void RemoveTempIdFromValueObject(Object valueObject, IEntityMetaData boMetaData, IMap<String, ITypeInfoItem> boNameToVoMember,
                IValueObjectConfig config)
        {
            ITypeInfoItem voIdMember = getVoIdMember(config, boMetaData, boNameToVoMember);
            Object nullEquivalentValue = NullEquivalentValueUtil.GetNullEquivalentValue(voIdMember.ElementType);
            voIdMember.SetValue(valueObject, nullEquivalentValue);
        }

        protected Object GetIdFromBusinessObject(Object businessObject, IEntityMetaData metaData)
        {
            return metaData.IdMember.GetValue(businessObject, false);
        }

        protected void SetTempIdToBusinessObject(Object businessObject, IEntityMetaData metaData)
        {
            PrimitiveMember idMember = metaData.IdMember;
            Object tempId = GetNextTempIdAs(idMember.ElementType);
            idMember.SetValue(businessObject, tempId);
            bosToRemoveTempIdFrom.Add(businessObject);
        }

        protected void RemoveTempIdFromBusinessObject(Object businessObject, IEntityMetaData metaData, ObjRef tempObjRef, IWritableCache cache)
        {
            PrimitiveMember idMember = metaData.IdMember;
            Object id = idMember.GetValue(businessObject);
            tempObjRef.RealType = metaData.EntityType;
            tempObjRef.IdNameIndex = ObjRef.PRIMARY_KEY_INDEX;
            tempObjRef.Id = id;
            cache.Remove(tempObjRef);
            idMember.SetValue(businessObject, null);
        }

        protected ITypeInfoItem getVoIdMember(IValueObjectConfig config, IEntityMetaData boMetaData, IMap<String, ITypeInfoItem> boNameToVoMember)
        {
            String boIdMemberName = boMetaData.IdMember.Name;
            return boNameToVoMember.Get(boIdMemberName);
        }

        protected void ClearObjectsWithTempIds(IWritableCache cache)
        {
            ISet<Object> bosToRemoveTempIdFrom = this.bosToRemoveTempIdFrom;
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            ISet<Object> vosToRemoveTempIdFrom = this.vosToRemoveTempIdFrom;
            if (vosToRemoveTempIdFrom.Count > 0)
            {
                foreach (Object vo in vosToRemoveTempIdFrom)
                {
                    IValueObjectConfig config = entityMetaDataProvider.GetValueObjectConfig(vo.GetType());
                    IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(config);
                    IEntityMetaData boMetaData = entityMetaDataProvider.GetMetaData(config.EntityType);
                    RemoveTempIdFromValueObject(vo, boMetaData, boNameToVoMember, config);
                }
                vosToRemoveTempIdFrom.Clear();
            }
            if (bosToRemoveTempIdFrom.Count > 0)
            {
                ObjRef objRef = new ObjRef();
                foreach (Object bo in bosToRemoveTempIdFrom)
                {
                    IEntityMetaData metaData = ((IEntityMetaDataHolder)bo).Get__EntityMetaData();
                    RemoveTempIdFromBusinessObject(bo, metaData, objRef, cache);
                }
                bosToRemoveTempIdFrom.Clear();
            }
        }

        protected Object GetNextTempIdAs(Type elementType)
        {
            if (nextTempId == long.MinValue)
            {
                nextTempId = -1;
            }
            return ConversionHelper.ConvertValueToType(elementType, nextTempId--);
        }

        protected PrimitiveMember SelectIdMember(IEntityMetaData referencedBOMetaData)
        {
            if (referencedBOMetaData == null)
            {
                throw new ArgumentException("Business object contains reference to object without metadata");
            }
            PrimitiveMember idMember = referencedBOMetaData.IdMember;
            if (referencedBOMetaData.GetAlternateIdCount() == 1)
            {
                idMember = referencedBOMetaData.AlternateIdMembers[0];
            }
            else if (referencedBOMetaData.GetAlternateIdCount() > 1)
            {
                // AgriLog specific solution for AlternateIdCount > 1
                foreach (PrimitiveMember alternateIdMember in referencedBOMetaData.AlternateIdMembers)
                {
                    if (alternateIdMember.Name.Equals("Buid"))
                    {
                        idMember = alternateIdMember;
                        break;
                    }
                }
            }
            return idMember;
        }

        protected IMap<String, ITypeInfoItem> GetTypeInfoMapForVo(IValueObjectConfig config)
        {
            IMap<String, ITypeInfoItem> typeInfoMap = typeToTypeInfoMap.Get(config.ValueType);
            if (typeInfoMap == null)
            {
                StringBuilder sb = new StringBuilder();
                typeInfoMap = new HashMap<String, ITypeInfoItem>();
                IEntityMetaData boMetaData = EntityMetaDataProvider.GetMetaData(config.EntityType);
                AddTypeInfoMapping(typeInfoMap, config, boMetaData.IdMember.Name, sb);
                if (boMetaData.VersionMember != null)
                {
                    AddTypeInfoMapping(typeInfoMap, config, boMetaData.VersionMember.Name, sb);
                }
                foreach (PrimitiveMember primitiveMember in boMetaData.PrimitiveMembers)
                {
                    AddTypeInfoMapping(typeInfoMap, config, primitiveMember.Name, sb);
                }
                foreach (RelationMember relationMember in boMetaData.RelationMembers)
                {
                    AddTypeInfoMapping(typeInfoMap, config, relationMember.Name, null);
                }
                typeToTypeInfoMap.Put(config.ValueType, typeInfoMap);
            }
            return typeInfoMap;
        }

        protected void AddTypeInfoMapping(IMap<String, ITypeInfoItem> typeInfoMap, IValueObjectConfig config, String boMemberName, StringBuilder sb)
        {
            String voMemberName = config.GetValueObjectMemberName(boMemberName);
            ITypeInfoItem voMember = TypeInfoProvider.GetHierarchicMember(config.ValueType, voMemberName);
            if (voMember == null)
            {
                return;
            }
            Type elementType = config.GetMemberType(voMemberName);
            if (elementType != null)
            {
                TypeInfoItem.SetEntityType(elementType, voMember, null);
            }
            typeInfoMap.Put(boMemberName, voMember);
            if (sb != null)
            {
                sb.Length = 0;
                String voSpecifiedName = sb.Append(voMemberName).Append("Specified").ToString();
                ITypeInfoItem voSpecifiedMember = TypeInfoProvider.GetHierarchicMember(config.ValueType, voSpecifiedName);
                if (voSpecifiedMember != null)
                {
                    sb.Length = 0;
                    String boSpecifiedName = sb.Append(boMemberName).Append("Specified").ToString();
                    typeInfoMap.Put(boSpecifiedName, voSpecifiedMember);
                }
            }
        }

        protected Object[] CopyPrimitives(Object businessObject, Object valueObject, IValueObjectConfig config, CopyDirection direction,
                IEntityMetaData businessObjectMetaData, IMap<String, ITypeInfoItem> boNameToVoMember)
        {
            PrimitiveMember[] primitiveMembers = AllPrimitiveMembers(businessObjectMetaData);
            Object[] primitives = new Object[businessObjectMetaData.PrimitiveMembers.Length];
            StringBuilder sb = new StringBuilder();
            for (int i = primitiveMembers.Length; i-- > 0; )
            {
                PrimitiveMember boMember = primitiveMembers[i];
                String boMemberName = boMember.Name;
                String voMemberName = config.GetValueObjectMemberName(boMemberName);
                sb.Length = 0;
                String boSpecifiedMemberName = sb.Append(boMemberName).Append("Specified").ToString();
                PrimitiveMember voMember = (PrimitiveMember) boNameToVoMember.Get(boMemberName);
                ITypeInfoItem voSpecifiedMember = boNameToVoMember.Get(boSpecifiedMemberName);
                bool isSpecified = true;
                if (config.IsIgnoredMember(voMemberName) || voMember == null)
                {
                    continue;
                }
                switch (direction)
                {
                    case CopyDirection.VO_TO_BO:
                        {
                            // Copy primitive from value object to business object
                            // TODO: Copy by value instead of copy by reference
                            if (voSpecifiedMember != null)
                            {
                                isSpecified = (bool)voSpecifiedMember.GetValue(valueObject);
                            }
                            if (!isSpecified)
                            {
                                continue;
                            }
                            Object value = voMember.GetValue(valueObject, false);
                            if (value != null && config.HoldsListType(voMemberName))
                            {
                                value = ListTypeHelper.UnpackListType(value);
                            }
                            value = ConvertPrimitiveValue(value, voMember.RealType, boMember);
                            // Do not 'kill' technical members except 'version' (for optimistic locking)
                            if (boMember.TechnicalMember && !boMember.Equals(businessObjectMetaData.VersionMember)
                                    && (value == null || value.Equals(boMember.NullEquivalentValue)))
                            {
                                continue;
                            }
                            if (value == null)
                            {
                                value = boMember.NullEquivalentValue;
                            }
                            boMember.SetValue(businessObject, value);
                            if (i < primitives.Length)
                            {
                                primitives[i] = value;
                            }
                            break;
                        }
                    case CopyDirection.BO_TO_VO:
                        {
                            // Copy primitive from business object to value object
                            // TODO: Copy by value instead of copy by reference
                            Object value = boMember.GetValue(businessObject, false);
                            isSpecified = value != null;
                            if (voSpecifiedMember != null)
                            {
                                voSpecifiedMember.SetValue(valueObject, isSpecified);
                            }
                            if (!isSpecified)
                            {
                                continue;
                            }
                            if (config.HoldsListType(voMemberName))
                            {
                                if (value is IEnumerable && !(value is String))
                                {
                                    value = ListTypeHelper.PackInListType((IEnumerable)value, voMember.RealType);
                                }
                            }
                            value = ConvertPrimitiveValue(value, boMember.ElementType, voMember);
                            if (voMember.TechnicalMember && (value == null || value.Equals(voMember.NullEquivalentValue)))
                            {
                                continue;
                            }
                            if (value == null)
                            {
                                value = boMember.NullEquivalentValue;
                            }
                            voMember.SetValue(valueObject, value);
                            break;
                        }
                    default:
                        throw RuntimeExceptionUtil.CreateEnumNotSupportedException(direction);
                }
            }
            return primitives;
        }

        protected PrimitiveMember[] AllPrimitiveMembers(IEntityMetaData businessObjectMetaData)
        {
            PrimitiveMember[] primitiveValueMembers = businessObjectMetaData.PrimitiveMembers;
            int technicalMemberCount = 1;
            if (businessObjectMetaData.VersionMember != null)
            {
                technicalMemberCount++;
            }
            PrimitiveMember[] primitiveMembers = new PrimitiveMember[primitiveValueMembers.Length + technicalMemberCount];
            Array.Copy(primitiveValueMembers, 0, primitiveMembers, 0, primitiveValueMembers.Length);
            int insertIndex = primitiveMembers.Length - technicalMemberCount;
            primitiveMembers[insertIndex++] = businessObjectMetaData.IdMember;
            if (businessObjectMetaData.VersionMember != null)
            {
                primitiveMembers[insertIndex++] = businessObjectMetaData.VersionMember;
            }
            return primitiveMembers;
        }

        protected Object ConvertPrimitiveValue(Object value, Type sourceElementType, Member targetMember)
        {
            if (value == null)
            {
                return null;
            }
            else if (value.GetType().IsArray && !typeof(String).Equals(targetMember.RealType)) // do not handle byte[]
            // or char[] to
            // String here
            {
                return ConvertPrimitiveValue(ListUtil.AnyToList(value), sourceElementType, targetMember);
            }
            else if (value is IEnumerable && !(value is String))
            {
                List<Object> result = new List<Object>();
                Type targetElementType;
                Type targetRealType = targetMember.RealType;

                if (targetRealType.IsArray)
                {
                    targetElementType = targetRealType.GetElementType();
                }
                else if (typeof(IEnumerable).IsAssignableFrom(targetMember.ElementType) && !typeof(String).IsAssignableFrom(targetMember.ElementType))
                {
                    targetElementType = sourceElementType;
                }
                else
                {
                    targetElementType = targetMember.ElementType;
                }
                foreach (Object item in (IEnumerable)value)
                {
                    Object convertedItem = ConversionHelper.ConvertValueToType(targetElementType, item);
                    result.Add(convertedItem);
                }
                if (targetRealType.IsArray)
                {
                    Array array = Array.CreateInstance(targetRealType.GetElementType(), result.Count);
                    for (int a = result.Count; a-- > 0; )
                    {
                        array.SetValue(result[a], a);
                    }
                    value = array;
                }
                else if (typeof(IEnumerable).IsAssignableFrom(targetRealType) && !typeof(String).Equals(targetRealType))
                {
                    value = ListUtil.CreateCollectionOfType(targetRealType, result.Count);
                    ListUtil.FillList(value, result);
                }
                else if (result.Count == 0)
                {
                    return null;
                }
                else if (result.Count == 1)
                {
                    return result[0];
                }
                else
                {
                    throw new ArgumentException("Cannot map '" + value.GetType() + "' of '" + sourceElementType + "' to '" + targetMember.RealType
                            + "' of '" + targetMember.ElementType + "'");
                }
            }
            else
            {
                value = ConversionHelper.ConvertValueToType(targetMember.RealType, value);
            }
            return value;
        }

        public Object GetMappedBusinessObject(IObjRef objRef)
        {
            return Cache.GetObject(objRef, CacheDirective.FailEarly);
        }

        public IList<Object> GetAllActiveBusinessObjects()
        {
            return voToBoMap.Values();
        }
    }
}
