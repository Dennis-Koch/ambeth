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

namespace De.Osthus.Minerva.Mock
{
    abstract public class AbstractMergeServiceMock : IMergeService, IInitializingBean
    {
        public virtual IPersistenceMock PersistenceMock { get; set; }

        public virtual IConversionHelper ConversionHelper { get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        public virtual IEntityMetaDataProvider EntityMetaDataProvider { get; set; }

        public virtual IEventDispatcher EventDispatcher { get; set; }

        public virtual IMergeController MergeController { get; set; }

        public virtual ITypeInfoProvider TypeInfoProvider { get; set; }

        protected int dcId = 0;

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(PersistenceMock, "PersistenceMock");
            ParamChecker.AssertNotNull(ConversionHelper, "ConversionHelper");
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
            ParamChecker.AssertNotNull(EventDispatcher, "EventDispatcher");
            ParamChecker.AssertNotNull(MergeController, "MergeController");
            ParamChecker.AssertNotNull(TypeInfoProvider, "TypeInfoProvider");
        }

        public IOriCollection Merge(ICUDResult cudResult, IMethodDescription methodDescription)
        {
            int localDcId;
            IDataChange dataChange;
            IList<IChangeContainer> allChanges = cudResult.AllChanges;
            IList<IObjRef> resultOriList = new List<IObjRef>(allChanges.Count);
            String changedBy = "anonymous";
            DateTime changedOn;

            Lock writeLock = PersistenceMock.GetWriteLock();
            writeLock.Lock();
            try
            {
                localDcId = ++dcId;
                changedOn = DateTime.Now.ToLocalTime();

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
                MergeController.ApplyChangesToOriginals(cudResult.GetOriginalRefs(), resultOriList, changedOn, changedBy);
                dataChange = new DataChangeEvent(inserts, updates, deletes, changedOn, false);
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

                ITypeInfo typeInfo = TypeInfoProvider.GetTypeInfo(entityType);

                List<Type> typesRelatingToThis = new List<Type>();
                IList<String> primitiveMemberNames = new List<String>();
                IList<String> relationMemberNames = new List<String>();

                FillMetaData(entityType, typesRelatingToThis, primitiveMemberNames, relationMemberNames);

                List<ITypeInfoItem> primitiveMembers = new List<ITypeInfoItem>();
                foreach (String primitiveMemberName in primitiveMemberNames)
                {
                    ITypeInfoItem primitiveMember = TypeInfoProvider.GetHierarchicMember(entityType, primitiveMemberName);
                    if (primitiveMember == null)
                    {
                        throw new Exception("No member with name '" + primitiveMemberName + "' found on entity type '" + entityType.FullName + "'");
                    }
                    primitiveMembers.Add(primitiveMember);
                }
                List<IRelationInfoItem> relationMembers = new List<IRelationInfoItem>();
                foreach (String relationMemberName in relationMemberNames)
                {
                    IRelationInfoItem relationMember = (IRelationInfoItem)typeInfo.GetMemberByName(relationMemberName);
                    if (relationMember == null)
                    {
                        throw new Exception("No member with name '" + relationMemberName + "' found on entity type '" + entityType.FullName + "'");
                    }
                    relationMembers.Add(relationMember);
                }
                EntityMetaData emd = new EntityMetaData();
                emd.EntityType = entityType;
                emd.IdMember = typeInfo.GetMemberByName("Id");
                emd.VersionMember = typeInfo.GetMemberByName("Version");
                emd.UpdatedByMember = typeInfo.GetMemberByName("UpdatedBy");
                emd.UpdatedOnMember = typeInfo.GetMemberByName("UpdatedOn");
                emd.CreatedByMember = typeInfo.GetMemberByName("CreatedBy");
                emd.CreatedOnMember = typeInfo.GetMemberByName("CreatedOn");

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

                emd.Initialize(EntityFactory);
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
