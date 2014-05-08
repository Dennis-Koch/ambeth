using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Windows.Documents;

namespace De.Osthus.Minerva.Mock
{
    public class CacheServiceMock : ICacheService, IPersistenceMock, IInitializingBean
    {
        public virtual IEntityMetaDataProvider EntityMetaDataProvider { get; set; }

        public virtual IObjRefHelper OriHelper { get; set; }

        public virtual IServiceByNameProvider ServiceByNameProvider { get; set; }

        protected readonly IDictionary<IObjRef, ILoadContainer> refToObjectDict = new Dictionary<IObjRef, ILoadContainer>();

        protected readonly Lock readLock, writeLock;

        public CacheServiceMock()
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
            ParamChecker.AssertNotNull(OriHelper, "OriHelper");
            ParamChecker.AssertNotNull(ServiceByNameProvider, "ServiceByNameProvider");
        }

        public Lock GetWriteLock()
        {
            return writeLock;
        }

        public IList<Object> GetAllIds<T>()
        {
            readLock.Lock();
            try
            {
                List<Object> ids = new List<Object>();
                IEnumerator<IObjRef> enumerator = refToObjectDict.Keys.GetEnumerator();
                while (enumerator.MoveNext())
                {
                    IObjRef objRef = enumerator.Current;
                    if (typeof(T).IsAssignableFrom(objRef.RealType))
                    {
                        ids.Add(objRef.Id);
                    }
                }
                return ids;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public void AddObject(IObjRef objRef, IPrimitiveUpdateItem[] primitiveUpdates, IRelationUpdateItem[] relationUpdates, String changedBy, DateTime changedOn)
        {
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objRef.RealType);

            Object[] primitives = new Object[metaData.PrimitiveMembers.Length];
            IObjRef[][] relations = new IObjRef[metaData.RelationMembers.Length][];
            for (int a = relations.Length; a-- > 0; )
            {
                relations[a] = ObjRef.EMPTY_ARRAY;
            }
            LoadContainer loadContainer = new LoadContainer();
            loadContainer.Reference = new ObjRef(objRef.RealType, objRef.Id, null);
            loadContainer.Primitives = primitives;
            loadContainer.Relations = relations;

            writeLock.Lock();
            try
            {
                refToObjectDict.Add(loadContainer.Reference, loadContainer);

                ChangeObject(objRef, primitiveUpdates, relationUpdates, changedBy, changedOn);
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        protected int GetPrimitiveMemberIndex(IEntityMetaData metaData, String primitiveMemberName)
        {
            ITypeInfoItem[] primitiveMembers = metaData.PrimitiveMembers;
            ITypeInfoItem member = metaData.GetMemberByName(primitiveMemberName);
            for (int b = primitiveMembers.Length; b-- > 0; )
            {
                if (primitiveMembers[b] == member)
                {
                    return b;
                }
            }
            throw new Exception("Primitive member with name '" + primitiveMemberName + "' not found on entity of type '" + metaData.EntityType);
        }

        public void ChangeObject(IObjRef objRef, IPrimitiveUpdateItem[] primitiveUpdates, IRelationUpdateItem[] relationUpdates, String changedBy, DateTime changedOn)
        {
            writeLock.Lock();
            try
            {
                ILoadContainer loadContainer = refToObjectDict[objRef];

                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objRef.RealType);

                Object[] primitives = loadContainer.Primitives;
                if (loadContainer.Reference.Version == null)
                {
                    if (metaData.CreatedByMember != null)
                    {
                        primitives[GetPrimitiveMemberIndex(metaData, metaData.CreatedByMember.Name)] = changedBy;
                    }
                    if (metaData.CreatedOnMember != null)
                    {
                        primitives[GetPrimitiveMemberIndex(metaData, metaData.CreatedOnMember.Name)] = changedOn;
                    }
                }
                else
                {
                    if (metaData.UpdatedByMember != null)
                    {
                        primitives[GetPrimitiveMemberIndex(metaData, metaData.UpdatedByMember.Name)] = changedBy;
                    }
                    if (metaData.UpdatedOnMember != null)
                    {
                        primitives[GetPrimitiveMemberIndex(metaData, metaData.UpdatedOnMember.Name)] = changedOn;
                    }
                }

                loadContainer.Reference.Version = objRef.Version;

                if (primitiveUpdates != null)
                {
                    ITypeInfoItem[] primitiveMembers = metaData.PrimitiveMembers;
                    for (int a = primitiveUpdates.Length; a-- > 0; )
                    {
                        IPrimitiveUpdateItem pui = primitiveUpdates[a];

                        ITypeInfoItem member = metaData.GetMemberByName(pui.MemberName);
                        for (int b = primitiveMembers.Length; b-- > 0; )
                        {
                            if (primitiveMembers[b] == member)
                            {
                                primitives[b] = pui.NewValue;
                                break;
                            }
                        }
                    }
                }
                if (relationUpdates != null)
                {
                    IObjRef[][] relations = loadContainer.Relations;
                    for (int a = relationUpdates.Length; a-- > 0; )
                    {
                        IRelationUpdateItem rui = relationUpdates[a];

                        int memberIndex = metaData.GetIndexByRelationName(rui.MemberName);

                        IObjRef[] relation = relations[memberIndex];

                        List<IObjRef> newRelation = new List<IObjRef>();
                        if (relation != null)
                        {
                            for (int b = relation.Length; b-- > 0; )
                            {
                                newRelation.Add(relation[b]);
                            }
                        }
                        if (rui.RemovedORIs != null)
                        {
                            for (int b = rui.RemovedORIs.Length; b-- > 0; )
                            {
                                newRelation.Remove(rui.RemovedORIs[b]);
                            }
                        }
                        if (rui.AddedORIs != null)
                        {
                            for (int b = rui.AddedORIs.Length; b-- > 0; )
                            {
                                newRelation.Add(rui.AddedORIs[b]);
                            }
                        }
                        if (newRelation.Count == 0)
                        {
                            relations[a] = ObjRef.EMPTY_ARRAY;
                        }
                        else
                        {
                            relations[memberIndex] = newRelation.ToArray();
                        }
                    }
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void RemoveObject(IObjRef objRef)
        {
            writeLock.Lock();
            try
            {
                ILoadContainer deletedContainer = refToObjectDict[objRef];

                refToObjectDict.Remove(objRef);
                Type deletedType = objRef.RealType;
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(deletedType);
                if (metaData.TypesRelatingToThis.Length == 0)
                {
                    return;
                }
                ISet<Type> typesRelatingToThis = new HashSet<Type>(metaData.TypesRelatingToThis);
                DictionaryExtension.Loop(refToObjectDict, delegate(IObjRef key, ILoadContainer value)
                {
                    if (!typesRelatingToThis.Contains(key.RealType))
                    {
                        // This object does not refer to instances of deleted type
                    }
                    IEntityMetaData typeRelatingMetaData = EntityMetaDataProvider.GetMetaData(key.RealType);
                    ITypeInfoItem[] relationMembers = typeRelatingMetaData.RelationMembers;
                    for (int a = relationMembers.Length; a-- > 0; )
                    {
                        ITypeInfoItem relationMember = relationMembers[a];
                        if (!deletedType.Equals(relationMember.ElementType))
                        {
                            continue;
                        }
                        IObjRef[] relationsOfMember = value.Relations[a];
                        if (relationsOfMember.Length == 0)
                        {
                            continue;
                        }
                        bool contains = false;
                        for (int b = relationsOfMember.Length; b-- > 0; )
                        {
                            IObjRef relationOfMember = relationsOfMember[b];
                            if (objRef.Equals(relationOfMember))
                            {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains)
                        {
                            continue;
                        }
                        if (relationsOfMember.Length == 1)
                        {
                            value.Relations[a] = ObjRef.EMPTY_ARRAY;
                            continue;
                        }
                        List<IObjRef> newRelationsOfMember = new List<IObjRef>();
                        for (int b = relationsOfMember.Length; b-- > 0; )
                        {
                            IObjRef relationOfMember = relationsOfMember[b];
                            if (!objRef.Equals(relationOfMember))
                            {
                                newRelationsOfMember.Add(relationOfMember);
                            }
                        }
                        value.Relations[a] = newRelationsOfMember.ToArray();
                    }
                });
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public IList<ILoadContainer> GetEntities(IList<IObjRef> orisToLoad)
        {
            readLock.Lock();
            try
            {
                List<ILoadContainer> loadContainers = new List<ILoadContainer>();

                for (int a = orisToLoad.Count; a-- > 0; )
                {
                    IObjRef oriToLoad = orisToLoad[a];

                    ILoadContainer loadContainer = DictionaryExtension.ValueOrDefault(refToObjectDict, oriToLoad);
                    if (loadContainer != null)
                    {
                        loadContainers.Add(loadContainer);
                    }
                }
                return loadContainers;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public IList<IObjRelationResult> GetRelations(IList<IObjRelation> objRelations)
        {
            throw new NotSupportedException("not yet implemented");
        }

        public IServiceResult GetORIsForServiceRequest(IServiceDescription serviceDescription)
        {
            Object service = ServiceByNameProvider.GetService(serviceDescription.ServiceName);

            if (service == null)
            {
                throw new Exception("Service with name '" + serviceDescription.ServiceName + "' not found");
            }
            Object result = serviceDescription.GetMethod(service.GetType()).Invoke(service, serviceDescription.Arguments);

            IList<IObjRef> objRefs = OriHelper.ExtractObjRefList(result, null);

            ServiceResult serviceResult = new ServiceResult();
            serviceResult.ObjRefs = objRefs;
            return serviceResult;
        }
    }
}
