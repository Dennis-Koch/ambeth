using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Merge
{
    public class EntityMetaDataCache : ClassExtendableContainer<IEntityMetaData>, IEntityMetaDataProvider, IValueObjectConfigExtendable,
        IInitializingBean
    {
        [Autowired]
        public IProxyFactory ProxyFactory { protected get; set; }

        [Autowired]
        public ValueObjectMap ValueObjectMap { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IXmlTypeHelper XmlTypeHelper { protected get; set; }

        protected readonly HashSet<Type> currentlyRequestedTypes = new HashSet<Type>();

        protected new IEntityMetaData alreadyHandled;

        public EntityMetaDataCache()
            : base("metaData", "type")
        {
            // Intended blank
        }

        public void AfterPropertiesSet()
        {
            alreadyHandled = ProxyFactory.CreateProxy<IEntityMetaData>();
        }

        public IEntityMetaData GetMetaData(Type entityType)
        {
            return GetMetaData(entityType, false);
        }

        public IEntityMetaData GetMetaData(Type entityType, bool tryOnly)
        {
            IEntityMetaData metaDataItem = GetExtension(entityType);
            if (metaDataItem != null)
            {
                if (Object.ReferenceEquals(metaDataItem, alreadyHandled))
                {
                    if (tryOnly)
                    {
                        return null;
                    }
                    throw new ArgumentException("No metadata found for entity of type " + entityType.FullName);
                }
                return metaDataItem;
            }
            List<Type> missingEntityTypes = new List<Type>(1);
            missingEntityTypes.Add(entityType);
            IList<IEntityMetaData> missingMetaData = GetMetaData(missingEntityTypes);
            if (missingMetaData.Count > 0)
            {
                return missingMetaData[0];
            }
            if (tryOnly)
            {
                return null;
            }
            throw new ArgumentException("No metadata found for entity of type " + entityType.Name);
        }

        protected IList<Type> AddLoadedMetaData(IList<Type> entityTypes, IList<IEntityMetaData> loadedMetaData)
        {
            HashSet<Type> cascadeMissingEntityTypes = null;
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                for (int a = loadedMetaData.Count; a-- > 0; )
                {
                    IEntityMetaData missingMetaDataItem = loadedMetaData[a];
                    Type entityType = missingMetaDataItem.EntityType;
                    if (GetExtension(entityType) != null)
                    {
                        continue;
                    }
                    Register(missingMetaDataItem, entityType);

                    foreach (IRelationInfoItem relationMember in missingMetaDataItem.RelationMembers)
                    {
                        Type relationMemberType = relationMember.ElementType;
                        if (!ContainsKey(relationMemberType))
                        {
                            if (cascadeMissingEntityTypes == null)
                            {
                                cascadeMissingEntityTypes = new HashSet<Type>();
                            }
                            cascadeMissingEntityTypes.Add(relationMemberType);
                        }
                    }
                }
                for (int a = entityTypes.Count; a-- > 0; )
                {
                    Type entityType = entityTypes[a];
                    if (!ContainsKey(entityType))
                    {
                        // add dummy items to ensure that this type does not
                        // get queried a second time
                        Register(alreadyHandled, entityType);
                    }
                }
                return cascadeMissingEntityTypes != null ? ListUtil.ToList(cascadeMissingEntityTypes) : null;
            }
        }

        public IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            List<IEntityMetaData> result = new List<IEntityMetaData>(entityTypes.Count);
            IList<Type> missingEntityTypes = null;
            for (int a = entityTypes.Count; a-- > 0; )
            {
                Type entityType = entityTypes[a];
                IEntityMetaData metaDataItem = GetExtension(entityType);
                if (metaDataItem != null)
                {
                    if (!Object.ReferenceEquals(metaDataItem, alreadyHandled))
                    {
                        result.Add(metaDataItem);
                    }
                    continue;
                }
                if (missingEntityTypes == null)
                {
                    missingEntityTypes = new List<Type>();
                }
                missingEntityTypes.Add(entityType);
            }
            if (missingEntityTypes == null)
            {
                return result;
            }
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            while (missingEntityTypes != null && missingEntityTypes.Count > 0)
            {
                IList<IEntityMetaData> loadedMetaData = entityMetaDataProvider.GetMetaData(missingEntityTypes);

                IList<Type> cascadeMissingEntityTypes = AddLoadedMetaData(missingEntityTypes, loadedMetaData);

                if (cascadeMissingEntityTypes != null && cascadeMissingEntityTypes.Count > 0)
                {
                    missingEntityTypes = cascadeMissingEntityTypes;
                }
                else
                {
                    missingEntityTypes.Clear();
                }
            }
            return GetMetaData(entityTypes);
        }

        public IList<Type> FindMappableEntityTypes()
        {
            List<Type> mappableEntities = new List<Type>();
            LinkedHashMap<Type, IValueObjectConfig> targetExtensionMap = new LinkedHashMap<Type, IValueObjectConfig>();
            ValueObjectMap.GetExtensions(targetExtensionMap);
            mappableEntities.AddRange(targetExtensionMap.KeySet());

            return mappableEntities;
        }

        public void RegisterValueObjectConfig(IValueObjectConfig config)
        {
            ValueObjectMap.Register(config, null);
        }

        public void UnregisterValueObjectConfig(IValueObjectConfig config)
        {
            ValueObjectMap.Unregister(config, null);
        }

        public IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            IValueObjectConfig config = ValueObjectMap.GetExtension(valueType);
            if (config == null)
            {
                config = EntityMetaDataProvider.GetValueObjectConfig(valueType);
                if (config != null)
                {
                    RegisterValueObjectConfig(config);
                }
            }

            return config;
        }

        public IValueObjectConfig GetValueObjectConfig(String xmlTypeName)
        {
            Type valueType = XmlTypeHelper.GetType(xmlTypeName);
            return GetValueObjectConfig(valueType);
        }

        public Type[] GetEntityPersistOrder()
        {
            return Type.EmptyTypes;
        }

        public IList<Type> GetValueObjectTypesByEntityType(Type entityType)
        {
            IList<Type> valueObjectTypes = ValueObjectMap.GetValueObjectTypesByEntityType(entityType);
            if (valueObjectTypes == null)
            {
                valueObjectTypes = Type.EmptyTypes;
            }
            return valueObjectTypes;
        }
    }
}