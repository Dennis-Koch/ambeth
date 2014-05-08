using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Merge
{
    public class EntityMetaDataFake : IEntityMetaDataProvider, IInitializingBean
    {
        protected readonly IDictionary<Type, IEntityMetaData> typeToEntityMetaData = new Dictionary<Type, IEntityMetaData>();

        protected readonly IDictionary<Type, IList<Type>> typeToRelatedTypesMap = new Dictionary<Type, IList<Type>>();

        [Autowired(Optional = true)]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IEntityMetaDataFiller EntityMetaDataFiller { protected get; set; }

        [Autowired]
        public IProperties Properties { protected get; set; }

        [Autowired(Optional = true)]
        public IProxyHelper ProxyHelper { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            if (EntityMetaDataFiller != null)
            {
                EntityMetaDataFiller.FillMetaData(this);
            }
        }

        public void AddMetaData(Type entityType, ITypeInfoItem idMember, ITypeInfoItem versionMember, ITypeInfoItem[] primitiveMembers,
            IRelationInfoItem[] relationMembers)
        {
            AddMetaData(entityType, idMember, versionMember, primitiveMembers, relationMembers, new ITypeInfoItem[0]);
        }

        public void AddMetaData(Type entityType, ITypeInfoItem idMember, ITypeInfoItem versionMember, ITypeInfoItem[] primitiveMembers,
                IRelationInfoItem[] relationMembers, ITypeInfoItem[] alternateIdMembers)
        {
            Type realEntityType = ProxyHelper != null ? ProxyHelper.GetRealType(entityType) : entityType;
            EntityMetaData metaData = new EntityMetaData();
            metaData.EntityType = entityType;
            metaData.RealType = realEntityType;
            metaData.IdMember = idMember;
            metaData.VersionMember = versionMember;

            // Order of setter calls is important
            metaData.PrimitiveMembers = primitiveMembers;
            metaData.AlternateIdMembers = alternateIdMembers;
            metaData.RelationMembers = relationMembers;

            this.typeToEntityMetaData.Add(metaData.EntityType, metaData);

            for (int i = relationMembers.Length; i-- > 0; )
            {
                IRelationInfoItem relationMember = relationMembers[i];
                TypeInfoItem.SetEntityType(relationMember.RealType, relationMember, Properties);
                UpdateRelations(relationMember.ElementType, entityType);
            }
            UpdateRelations(entityType, null);
            metaData.Initialize(EntityFactory);
        }

        protected ITypeInfoItem[] CreateMembers(ITypeInfo typeInfo, String[] memberNames)
        {
            ITypeInfoItem[] members = new ITypeInfoItem[memberNames.Length];
            for (int a = memberNames.Length; a-- > 0; )
            {
                members[a] = typeInfo.GetMemberByName(memberNames[a]);
            }
            return members;
        }

        protected IRelationInfoItem[] CreateRelationMembers(ITypeInfo typeInfo, String[] memberNames)
        {
            IRelationInfoItem[] members = new IRelationInfoItem[memberNames.Length];
            for (int a = memberNames.Length; a-- > 0; )
            {
                members[a] = (IRelationInfoItem)typeInfo.GetMemberByName(memberNames[a]);
            }
            return members;
        }

        protected void UpdateRelations(Type entityType, Type typeToAdd)
        {
            IList<Type> relateToType = DictionaryExtension.ValueOrDefault(this.typeToRelatedTypesMap, entityType);
            if (relateToType == null)
            {
                relateToType = new List<Type>();
                this.typeToRelatedTypesMap.Add(entityType, relateToType);
            }

            if (typeToAdd != null)
            {
                relateToType.Add(typeToAdd);
            }

            EntityMetaData entityTypeMetaData = (EntityMetaData)DictionaryExtension.ValueOrDefault(this.typeToEntityMetaData, entityType);
            if (entityTypeMetaData != null)
            {
                entityTypeMetaData.TypesRelatingToThis = ListUtil.ToArray(relateToType);
            }
        }

        public IEntityMetaData GetMetaData(Type entityType)
        {
            return GetMetaData(entityType, false);
        }

        public IEntityMetaData GetMetaData(Type entityType, bool tryOnly)
        {
            Type realEntityType = this.ProxyHelper.GetRealType(entityType);
            IEntityMetaData metaData = DictionaryExtension.ValueOrDefault(this.typeToEntityMetaData, realEntityType);
            if (metaData != null || tryOnly)
            {
                return metaData;
            }
            throw new Exception("No metadata found for entity of type " + realEntityType.Name);
        }

        public IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            IList<IEntityMetaData> entityMetaData = new List<IEntityMetaData>();
            foreach (Type entityType in entityTypes)
            {
                Type realEntityType = this.ProxyHelper.GetRealType(entityType);
                IEntityMetaData metaDataItem = DictionaryExtension.ValueOrDefault(this.typeToEntityMetaData, realEntityType);

                if (metaDataItem != null)
                {
                    entityMetaData.Add(metaDataItem);
                }
            }
            return entityMetaData;
        }

        public IList<Type> FindMappableEntityTypes()
        {
            throw new NotImplementedException("Not implemented");
        }

        public IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            throw new NotImplementedException("Not implemented");
        }

        public IValueObjectConfig GetValueObjectConfig(String xmlTypeName)
        {
            throw new NotImplementedException("Not implemented");
        }

        public Type[] GetBusinessObjectSaveOrder()
        {
            return Type.EmptyTypes;
        }

        public Type[] GetEntityPersistOrder()
        {
            return Type.EmptyTypes;
        }

        public IList<Type> GetValueObjectTypesByEntityType(Type entityType)
        {
            throw new NotImplementedException("Not implemented");
        }
    }
}
