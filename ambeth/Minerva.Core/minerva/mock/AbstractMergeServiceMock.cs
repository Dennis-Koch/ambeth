using System;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Merge.Model;
using System.Collections.Generic;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Minerva.Mock
{
    abstract public class AbstractMergeServiceMock : IMergeService
    {
        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IEventDispatcher EventDispatcher { protected get; set; }

        [Autowired]
        public IMemberTypeProvider MemberTypeProvider { protected get; set; }

        [Autowired]
        public IMergeController MergeController { protected get; set; }

        [Autowired]
        public IPersistenceMock PersistenceMock { protected get; set; }

        protected int dcId = 0;
        
        public IOriCollection Merge(ICUDResult cudResult, IMethodDescription methodDescription)
        {
            int localDcId;
            IDataChange dataChange;
            IList<IChangeContainer> allChanges = cudResult.AllChanges;
            IList<IObjRef> resultOriList = new List<IObjRef>(allChanges.Count);
            String changedBy = "anonymous";
            long changedOn;

            Lock writeLock = PersistenceMock.GetWriteLock();
            writeLock.Lock();
            try
            {
                localDcId = ++dcId;
                changedOn = DateTimeUtil.CurrentTimeMillis();

                IList<IDataChangeEntry> inserts = new List<IDataChangeEntry>();
                IList<IDataChangeEntry> updates = new List<IDataChangeEntry>();
                IList<IDataChangeEntry> deletes = new List<IDataChangeEntry>();
                for (int a = 0, size = allChanges.Count; a < size; a++)
                {
                    IChangeContainer changeContainer = allChanges[a];
                    IObjRef reference = changeContainer.Reference;
                    if (changeContainer is DeleteContainer)
                    {
                        PersistenceMock.RemoveObject(reference);
                        resultOriList.Add(null);
                        deletes.Add(new DataChangeEntry(reference.RealType, reference.IdNameIndex, reference.Id, reference.Version));
                    }
                    else if (changeContainer is UpdateContainer)
                    {
                        resultOriList.Add(reference);
                        reference.Version = ((int)reference.Version) + 1;
                        PersistenceMock.ChangeObject(reference, ((UpdateContainer)changeContainer).Primitives, ((UpdateContainer)changeContainer).Relations, changedBy, changedOn);
                        updates.Add(new DataChangeEntry(reference.RealType, reference.IdNameIndex, reference.Id, reference.Version));
                    }
                    else if (changeContainer is CreateContainer)
                    {
                        Object newId = AcquireIdForEntityType(reference.RealType);
                        if (newId == null)
                        {
                            throw new Exception("Id must be valid");
                        }
                        IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(reference.RealType);
                        newId = ConversionHelper.ConvertValueToType(metaData.IdMember.ElementType, newId);
                        reference.Id = newId;
                        reference.Version = 1;
                        ((IDirectObjRef)reference).Direct = null;
                        resultOriList.Add(reference);
                        PersistenceMock.AddObject(reference, ((CreateContainer)changeContainer).Primitives, ((CreateContainer)changeContainer).Relations, changedBy, changedOn);
                        inserts.Add(new DataChangeEntry(reference.RealType, ObjRef.PRIMARY_KEY_INDEX, reference.Id, reference.Version));
                    }
                }
                OriCollection oriColl = new OriCollection(resultOriList);
                oriColl.ChangedBy = changedBy;
                oriColl.ChangedOn = changedOn;
                MergeController.ApplyChangesToOriginals(cudResult, oriColl, null);
                dataChange = new DataChangeEvent(inserts, updates, deletes, DateTimeUtil.ConvertJavaMillisToDateTime(changedOn), false);
            }
            finally
            {
                writeLock.Unlock();
            }
            EventDispatcher.DispatchEvent(dataChange, dataChange.ChangeTime, localDcId);

            OriCollection oriCollection = new OriCollection(resultOriList);
            oriCollection.ChangedBy = changedBy;
            oriCollection.ChangedOn = changedOn;
            return oriCollection;
        }

        public IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            List<IEntityMetaData> result = new List<IEntityMetaData>();
            for (int a = entityTypes.Count; a-- > 0; )
            {
                Type entityType = entityTypes[a];

                List<Type> typesRelatingToThis = new List<Type>();
                IList<String> primitiveMemberNames = new List<String>();
                IList<String> relationMemberNames = new List<String>();

                FillMetaData(entityType, typesRelatingToThis, primitiveMemberNames, relationMemberNames);

                List<PrimitiveMember> primitiveMembers = new List<PrimitiveMember>();
                foreach (String primitiveMemberName in primitiveMemberNames)
                {
                    PrimitiveMember primitiveMember = MemberTypeProvider.GetPrimitiveMember(entityType, primitiveMemberName);
                    if (primitiveMember == null)
                    {
                        throw new Exception("No member with name '" + primitiveMemberName + "' found on entity type '" + entityType.FullName + "'");
                    }
                    primitiveMembers.Add(primitiveMember);
                }
                List<RelationMember> relationMembers = new List<RelationMember>();
                foreach (String relationMemberName in relationMemberNames)
                {
                    RelationMember relationMember = MemberTypeProvider.GetRelationMember(entityType, relationMemberName);
                    if (relationMember == null)
                    {
                        throw new Exception("No member with name '" + relationMemberName + "' found on entity type '" + entityType.FullName + "'");
                    }
                    relationMembers.Add(relationMember);
                }
                EntityMetaData emd = new EntityMetaData();
                emd.EntityType = entityType;
                emd.IdMember = MemberTypeProvider.GetPrimitiveMember(entityType, "Id");
                emd.VersionMember = MemberTypeProvider.GetPrimitiveMember(entityType, "Version");
                emd.UpdatedByMember = MemberTypeProvider.GetPrimitiveMember(entityType, "UpdatedBy");
                emd.UpdatedOnMember = MemberTypeProvider.GetPrimitiveMember(entityType, "UpdatedOn");
                emd.CreatedByMember = MemberTypeProvider.GetPrimitiveMember(entityType, "CreatedBy");
                emd.CreatedOnMember = MemberTypeProvider.GetPrimitiveMember(entityType, "CreatedOn");

                if (emd.UpdatedByMember != null)
                {
                    primitiveMembers.Add(emd.UpdatedByMember);
                }
                if (emd.UpdatedOnMember != null)
                {
                    primitiveMembers.Add(emd.UpdatedOnMember);
                }
                if (emd.CreatedByMember != null)
                {
                    primitiveMembers.Add(emd.CreatedByMember);
                }
                if (emd.CreatedOnMember != null)
                {
                    primitiveMembers.Add(emd.CreatedOnMember);
                }
                emd.PrimitiveMembers = primitiveMembers.ToArray();
                emd.RelationMembers = relationMembers.ToArray();
                emd.TypesRelatingToThis = typesRelatingToThis.ToArray();

                emd.Initialize(CacheModification, EntityFactory);
                result.Add(emd);
            }
            return result;
        }

        public IValueObjectConfig GetValueObjectConfig(Type valueObjectType)
        {
            return EntityMetaDataProvider.GetValueObjectConfig(valueObjectType);
        }

        protected abstract Object AcquireIdForEntityType(Type entityType);

        protected abstract void FillMetaData(Type entityType, IList<Type> typesRelatingToThis, IList<String> primitiveMembers,
            IList<String> relationMembers);
    }
}
