using System;
using System.Collections.Generic;
using System.Text;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml;

namespace De.Osthus.Ambeth.Merge
{
    public class IndependentEntityMetaDataClient : ClassExtendableContainer<IEntityMetaData>, IEntityMetaDataProvider, IValueObjectConfigExtendable, IEntityMetaDataExtendable
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ValueObjectMap ValueObjectMap { protected get; set; }

        [Autowired(Optional = true)]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        [Autowired]
        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        [Autowired]
        public IXmlTypeHelper XmlTypeHelper { protected get; set; }

        protected Type[] businessObjectSaveOrder;

        protected readonly IMap<Type, IMap<String, ITypeInfoItem>> typeToPropertyMap = new HashMap<Type, IMap<String, ITypeInfoItem>>();

        protected readonly Lock readLock, writeLock;

        public IndependentEntityMetaDataClient()
            : base("entity meta data", "entity class")
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

        protected void Initialize()
        {
            HashMap<Type, IISet<Type>> typeRelatedByTypes = new HashMap<Type, IISet<Type>>();
            IdentityHashSet<IEntityMetaData> extensions = new IdentityHashSet<IEntityMetaData>(GetExtensions().Values());
            foreach (IEntityMetaData metaData in extensions)
            {
                foreach (IRelationInfoItem relationMember in metaData.RelationMembers)
                {
                    AddTypeRelatedByTypes(typeRelatedByTypes, metaData.EntityType, relationMember.ElementType);
                }
            }
            foreach (IEntityMetaData metaData in extensions)
            {
                IISet<Type> relatedByTypes = typeRelatedByTypes.Get(metaData.EntityType);
                if (relatedByTypes == null)
                {
                    relatedByTypes = new CHashSet<Type>();
                }
                ((EntityMetaData)metaData).TypesRelatingToThis = relatedByTypes.ToArray();
                ((EntityMetaData)metaData).Initialize(EntityFactory);
            }
        }

        protected void InitializeValueObjectMapping()
        {
            Lock writeLock = this.writeLock;
            writeLock.Lock();
            try
            {
                this.businessObjectSaveOrder = null;

                HashMap<Type, IISet<Type>> boTypeToBeforeBoTypes = new HashMap<Type, IISet<Type>>();
                HashMap<Type, IISet<Type>> boTypeToAfterBoTypes = new HashMap<Type, IISet<Type>>();

                foreach (Entry<Type, IValueObjectConfig> entry in ValueObjectMap.GetExtensions())
                {
                    IValueObjectConfig voConfig = entry.Value;
                    Type entityType = voConfig.EntityType;
                    Type valueType = voConfig.ValueType;
                    IEntityMetaData metaData = GetMetaData(entityType);

                    if (metaData == null)
                    {
                        // Currently no bo metadata found. We can do nothing here
                        return;
                    }
                    IMap<String, ITypeInfoItem> boNameToVoMember = GetTypeInfoMapForVo(valueType);

                    foreach (IRelationInfoItem boMember in metaData.RelationMembers)
                    {
                        String boMemberName = boMember.Name;
                        String voMemberName = voConfig.GetValueObjectMemberName(boMemberName);
                        ITypeInfoItem voMember = boNameToVoMember.Get(boMemberName);
                        if (voConfig.IsIgnoredMember(voMemberName) || voMember == null)
                        {
                            continue;
                        }
                        Type voMemberRealType = voMember.RealType;
                        if (voConfig.HoldsListType(voMember.Name))
                        {
                            IPropertyInfo[] properties = PropertyInfoProvider.GetProperties(voMemberRealType);
                            if (properties.Length != 1)
                            {
                                throw new ArgumentException("ListTypes must have exactly one property");
                            }
                            voMemberRealType = TypeInfoProvider.GetMember(voMemberRealType, properties[0]).RealType;
                        }
                        if (!ImmutableTypeSet.IsImmutableType(voMemberRealType))
                        {
                            // vo member is either a list or a single direct relation to another VO
                            // This implies that a potential service can handle both VO types as new objects at once
                            continue;
                        }
                        // vo member only holds a id reference which implies that the related VO has to be persisted first to
                        // contain an id which can be referred to. But we do NOT know the related VO here, but we know
                        // the related BO where ALL potential VOs will be derived from:
                        Type boMemberElementType = boMember.ElementType;

                        if (Object.Equals(entityType, boMemberElementType))
                        {
                            continue;
                        }

                        AddBoTypeAfter(entityType, boMemberElementType, boTypeToBeforeBoTypes, boTypeToAfterBoTypes);
                        AddBoTypeBefore(entityType, boMemberElementType, boTypeToBeforeBoTypes, boTypeToAfterBoTypes);
                    }
                }
                List<Type> businessObjectSaveOrder = new List<Type>();

                foreach (Type boType in boTypeToBeforeBoTypes.KeySet())
                {
                    // BeforeBoType are types which have to be saved BEFORE saving the boType
                    bool added = false;
                    for (int a = 0, size = businessObjectSaveOrder.Count; a < size; a++)
                    {
                        Type orderedBoType = businessObjectSaveOrder[a];

                        // OrderedBoType is the type currently inserted at the correct position in the save order - as far as the keyset
                        // has been traversed, yet

                        ISet<Type> typesBeforeOrderedType = boTypeToBeforeBoTypes.Get(orderedBoType);
                        // typesBeforeOrderedType are types which have to be

                        bool orderedHasToBeAfterCurrent = typesBeforeOrderedType != null && typesBeforeOrderedType.Contains(boType);

                        if (!orderedHasToBeAfterCurrent)
                        {
                            // our boType has nothing to do with the orderedBoType. So we let is be at it is
                            continue;
                        }
                        businessObjectSaveOrder.Insert(a, boType);
                        added = true;
                        break;
                    }
                    if (!added)
                    {
                        businessObjectSaveOrder.Add(boType);
                    }
                }
                foreach (Type boType in boTypeToAfterBoTypes.KeySet())
                {
                    if (boTypeToBeforeBoTypes.ContainsKey(boType))
                    {
                        // already handled in the previous loop
                        continue;
                    }
                    bool added = false;
                    for (int a = businessObjectSaveOrder.Count; a-- > 0; )
                    {
                        Type orderedBoType = businessObjectSaveOrder[a];

                        // OrderedBoType is the type currently inserted at the correct position in the save order - as far as the keyset
                        // has been traversed, yet

                        ISet<Type> typesBeforeOrderedType = boTypeToBeforeBoTypes.Get(orderedBoType);

                        bool orderedHasToBeAfterCurrent = typesBeforeOrderedType != null && typesBeforeOrderedType.Contains(boType);

                        if (!orderedHasToBeAfterCurrent)
                        {
                            // our boType has nothing to do with the orderedBoType. So we let it be as it is
                            continue;
                        }
                        businessObjectSaveOrder.Insert(a, boType);
                        added = true;
                        break;
                    }
                    if (!added)
                    {
                        businessObjectSaveOrder.Add(boType);
                    }
                }
                this.businessObjectSaveOrder = businessObjectSaveOrder.ToArray();
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        protected void AddBoTypeBefore(Type boType, Type beforeBoType, IMap<Type, IISet<Type>> boTypeToBeforeBoTypes,
                IMap<Type, IISet<Type>> boTypeToAfterBoTypes)
        {
            IISet<Type> beforeBoTypes = boTypeToBeforeBoTypes.Get(boType);
            if (beforeBoTypes == null)
            {
                beforeBoTypes = new CHashSet<Type>();
                boTypeToBeforeBoTypes.Put(boType, beforeBoTypes);
            }
            beforeBoTypes.Add(beforeBoType);

            ISet<Type> afterBoTypes = boTypeToAfterBoTypes.Get(boType);
            if (afterBoTypes != null)
            {
                // Add boType as a after BO for all BOs which are BEFORE afterBoType (similar to: if 1<3 and 3<4 then 1<4)
                foreach (Type afterBoType in afterBoTypes)
                {
                    AddBoTypeBefore(afterBoType, beforeBoType, boTypeToBeforeBoTypes, boTypeToAfterBoTypes);
                }
            }
        }

        protected void AddBoTypeAfter(Type boType, Type afterBoType, IMap<Type, IISet<Type>> boTypeBeforeBoTypes,
                IMap<Type, IISet<Type>> boTypeToAfterBoTypes)
        {
            IISet<Type> afterBoTypes = boTypeToAfterBoTypes.Get(afterBoType);
            if (afterBoTypes == null)
            {
                afterBoTypes = new CHashSet<Type>();
                boTypeToAfterBoTypes.Put(afterBoType, afterBoTypes);
            }
            afterBoTypes.Add(boType);

            ISet<Type> beforeBoTypes = boTypeBeforeBoTypes.Get(afterBoType);
            if (beforeBoTypes != null)
            {
                // Add afterBoType as a after BO for all BOs which are BEFORE boType (similar to: if 1<3 and 3<4 then 1<4)
                foreach (Type beforeBoType in beforeBoTypes)
                {
                    AddBoTypeAfter(beforeBoType, afterBoType, boTypeBeforeBoTypes, boTypeToAfterBoTypes);
                }
            }
        }

        protected static void AddTypeRelatedByTypes(IMap<Type, IISet<Type>> typeRelatedByTypes, Type relating, Type relatedTo)
        {
            IISet<Type> relatedByTypes = typeRelatedByTypes.Get(relatedTo);
            if (relatedByTypes == null)
            {
                relatedByTypes = new CHashSet<Type>();
                typeRelatedByTypes.Put(relatedTo, relatedByTypes);
            }
            relatedByTypes.Add(relating);
        }

        public IEntityMetaData GetMetaData(Type entityType)
        {
            return GetMetaData(entityType, false);
        }

        public IEntityMetaData GetMetaData(Type entityType, bool tryOnly)
        {
            IEntityMetaData metaDataItem;
            Lock readLock = this.readLock;
            readLock.Lock();
            try
            {
                metaDataItem = GetExtension(entityType);
            }
            finally
            {
                readLock.Unlock();
            }
            if (metaDataItem == null)
            {
                if (tryOnly)
                {
                    return null;
                }
                throw new ArgumentException("No metadata found for entity of type " + entityType);
            }
            return metaDataItem;
        }

        public IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            List<IEntityMetaData> entityMetaData = new List<IEntityMetaData>(entityTypes.Count);
            Lock readLock = this.readLock;
            readLock.Lock();
            try
            {
                List<Type> notFoundEntityTypes = new List<Type>();
                foreach (Type entityType in entityTypes)
                {
                    IEntityMetaData metaDataItem = GetExtension(entityType);

                    if (metaDataItem != null)
                    {
                        entityMetaData.Add(metaDataItem);
                    }
                    else
                    {
                        notFoundEntityTypes.Add(entityType);
                    }
                }
                if (notFoundEntityTypes.Count > 0 && Log.WarnEnabled)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.Append("No metadata found for ").Append(notFoundEntityTypes.Count).Append(" type(s):");
                    foreach (Type notFoundType in notFoundEntityTypes)
                    {
                        sb.Append("\t\n").Append(notFoundType.FullName);
                    }
                    Log.Warn(sb.ToString());
                }
            }
            finally
            {
                readLock.Unlock();
            }
            return entityMetaData;
        }

        public IList<Type> FindMappableEntityTypes()
        {
            List<Type> mappableEntities = new List<Type>();
            LinkedHashMap<Type, IValueObjectConfig> targetExtensionMap = new LinkedHashMap<Type, IValueObjectConfig>();
            ValueObjectMap.GetExtensions(targetExtensionMap);
            mappableEntities.AddRange(targetExtensionMap.KeySet());

            return mappableEntities;
        }

        public IMap<String, ITypeInfoItem> GetTypeInfoMapForVo(Type valueType)
        {
            IValueObjectConfig config = GetValueObjectConfig(valueType);
            if (config == null)
            {
                return null;
            }
            IMap<String, ITypeInfoItem> typeInfoMap = typeToPropertyMap.Get(valueType);
            if (typeInfoMap == null)
            {
                typeInfoMap = new HashMap<String, ITypeInfoItem>();
                IEntityMetaData boMetaData = GetMetaData(config.EntityType);
                StringBuilder sb = new StringBuilder();

                AddTypeInfoMapping(typeInfoMap, config, boMetaData.IdMember.Name, sb);
                if (boMetaData.VersionMember != null)
                {
                    AddTypeInfoMapping(typeInfoMap, config, boMetaData.VersionMember.Name, sb);
                }
                foreach (ITypeInfoItem primitiveMember in boMetaData.PrimitiveMembers)
                {
                    AddTypeInfoMapping(typeInfoMap, config, primitiveMember.Name, sb);
                }
                foreach (ITypeInfoItem relationMember in boMetaData.RelationMembers)
                {
                    AddTypeInfoMapping(typeInfoMap, config, relationMember.Name, null);
                }

                if (!typeToPropertyMap.PutIfNotExists(config.ValueType, typeInfoMap))
                {
                    throw new Exception("Key already exists " + config.ValueType);
                }
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
            typeInfoMap.Put(boMemberName, voMember);
            if (sb == null)
            {
                return;
            }
            sb.Length = 0;
            String voSpecifiedName = sb.Append(voMemberName).Append("Specified").ToString();
            ITypeInfoItem voSpecifiedMember = TypeInfoProvider.GetHierarchicMember(config.ValueType, voSpecifiedName);
            if (voSpecifiedMember == null)
            {
                return;
            }
            sb.Length = 0;
            String boSpecifiedName = sb.Append(boMemberName).Append("Specified").ToString();
            typeInfoMap.Put(boSpecifiedName, voSpecifiedMember);
        }

        public void RegisterEntityMetaData(IEntityMetaData entityMetaData)
        {
            RegisterEntityMetaData(entityMetaData, entityMetaData.EntityType);
        }

        public void RegisterEntityMetaData(IEntityMetaData entityMetaData, Type entityType)
        {
            Lock writeLock = this.writeLock;
            writeLock.Lock();
            try
            {
                Register(entityMetaData, entityType);
                Initialize();
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void UnregisterEntityMetaData(IEntityMetaData entityMetaData)
        {
            UnregisterEntityMetaData(entityMetaData, entityMetaData.EntityType);
        }

        public void UnregisterEntityMetaData(IEntityMetaData entityMetaData, Type entityType)
        {
            Lock writeLock = this.writeLock;
            writeLock.Lock();
            try
            {
                Unregister(entityMetaData, entityType);
                Initialize();
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void RegisterValueObjectConfig(IValueObjectConfig config)
        {
            ValueObjectMap.Register(config, config.ValueType);
        }

        public void UnregisterValueObjectConfig(IValueObjectConfig config)
        {
            ValueObjectMap.Unregister(config, config.ValueType);
        }

        public IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            return ValueObjectMap.GetExtension(valueType);
        }

        public IValueObjectConfig GetValueObjectConfig(String xmlTypeName)
        {
            Type valueType = XmlTypeHelper.GetType(xmlTypeName);
            return GetValueObjectConfig(valueType);
        }

        public Type[] GetEntityPersistOrder()
        {
            return businessObjectSaveOrder;
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
