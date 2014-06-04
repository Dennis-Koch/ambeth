﻿using System;
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
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Event;
using System.Reflection;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Merge
{
    public class EntityMetaDataProvider : ClassExtendableContainer<IEntityMetaData>, IEntityMetaDataProvider, IEntityMetaDataExtendable, IEntityLifecycleExtendable, IValueObjectConfigExtendable, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        [Autowired(Optional = true)]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IEventDispatcher EventDispatcher { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        [Autowired]
        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        [Autowired]
        public IXmlTypeHelper XmlTypeHelper { protected get; set; }

        [Autowired]
        public ValueObjectMap ValueObjectMap { protected get; set; }

        protected Type[] businessObjectSaveOrder;

        protected readonly IMap<Type, IMap<String, ITypeInfoItem>> typeToPropertyMap = new HashMap<Type, IMap<String, ITypeInfoItem>>();

        protected readonly ClassExtendableListContainer<IEntityLifecycleExtension> entityLifecycleExtensions = new ClassExtendableListContainer<IEntityLifecycleExtension>(
            "entityLifecycleExtension", "entityType");

        public EntityMetaDataProvider()
            : base("entity meta data", "entity class")
        {
            // Intended blank
        }

        public void AfterPropertiesSet()
        {
        }

        protected void AddTypeRelatedByTypes(IMap<Type, IISet<Type>> typeRelatedByTypes, Type relating, Type relatedTo)
        {
            IISet<Type> relatedByTypes = typeRelatedByTypes.Get(relatedTo);
            if (relatedByTypes == null)
            {
                relatedByTypes = new CHashSet<Type>();
                typeRelatedByTypes.Put(relatedTo, relatedByTypes);
            }
            relatedByTypes.Add(relating);
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

        public IEntityMetaData GetMetaData(Type entityType)
        {
            return GetMetaData(entityType, false);
        }

        public IEntityMetaData GetMetaData(Type entityType, bool tryOnly)
        {
            IEntityMetaData metaDataItem = GetExtension(entityType);

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
                notFoundEntityTypes.Sort();
                StringBuilder sb = new StringBuilder();
                sb.Append("No metadata found for ").Append(notFoundEntityTypes.Count).Append(" type(s):");
                foreach (Type notFoundType in notFoundEntityTypes)
                {
                    sb.Append("\t\n").Append(notFoundType.FullName);
                }
                Log.Warn(sb.ToString());
            }
            return entityMetaData;
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

        public IList<Type> GetValueObjectTypesByEntityType(Type entityType)
        {
            IList<Type> valueObjectTypes = ValueObjectMap.GetValueObjectTypesByEntityType(entityType);
            if (valueObjectTypes == null)
            {
                valueObjectTypes = Type.EmptyTypes;
            }
            return valueObjectTypes;
        }

        public void RegisterEntityMetaData(IEntityMetaData entityMetaData)
        {
            RegisterEntityMetaData(entityMetaData, entityMetaData.EntityType);
        }

        public void RegisterEntityMetaData(IEntityMetaData entityMetaData, Type entityType)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                Register(entityMetaData, entityType);
                Initialize();
            }
            EventDispatcher.DispatchEvent(new EntityMetaDataAddedEvent(entityType));
        }

        public void UnregisterEntityMetaData(IEntityMetaData entityMetaData)
        {
            UnregisterEntityMetaData(entityMetaData, entityMetaData.EntityType);
        }

        public void UnregisterEntityMetaData(IEntityMetaData entityMetaData, Type entityType)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                Unregister(entityMetaData, entityType);
                Initialize();
            }
            EventDispatcher.DispatchEvent(new EntityMetaDataRemovedEvent(entityType));
        }

        public override void Register(IEntityMetaData extension, Type key)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                base.Register(extension, key);
                UpdateEntityMetaDataWithLifecycleExtensions(extension);
            }
        }

        public override void Unregister(IEntityMetaData extension, Type key)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                base.Unregister(extension, key);
                CleanEntityMetaDataFromLifecycleExtensions(extension);
            }
        }

        public void RegisterEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension, Type entityType)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                entityLifecycleExtensions.Register(entityLifecycleExtension, entityType);
                UpdateAllEntityMetaDataWithLifecycleExtensions();
            }
        }

        public void UnregisterEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension, Type entityType)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                entityLifecycleExtensions.Unregister(entityLifecycleExtension, entityType);
                UpdateAllEntityMetaDataWithLifecycleExtensions();
            }
        }

        protected void CleanEntityMetaDataFromLifecycleExtensions(IEntityMetaData entityMetaData)
        {
            ((EntityMetaData)entityMetaData).EntityLifecycleExtensions = null;
        }

        protected void UpdateEntityMetaDataWithLifecycleExtensions(IEntityMetaData entityMetaData)
        {
            IList<IEntityLifecycleExtension> extensionList = entityLifecycleExtensions.GetExtensions(entityMetaData.EntityType);
            List<IEntityLifecycleExtension> allExtensions = new List<IEntityLifecycleExtension>();
            if (extensionList != null)
            {
                allExtensions.AddRange(extensionList);
            }
            List<MethodInfo> prePersistMethods = new List<MethodInfo>();
            FillMethodsAnnotatedWith(entityMetaData.RealType, prePersistMethods, typeof(PrePersistAttribute));

            List<MethodInfo> postLoadMethods = new List<MethodInfo>();
            FillMethodsAnnotatedWith(entityMetaData.RealType, postLoadMethods, typeof(PostLoadAttribute));

            foreach (MethodInfo prePersistMethod in prePersistMethods)
            {
                PrePersistMethodLifecycleExtension extension = BeanContext.RegisterAnonymousBean<PrePersistMethodLifecycleExtension>()
                        .PropertyValue("Method", prePersistMethod).Finish();
                allExtensions.Add(extension);
            }
            foreach (MethodInfo postLoadMethod in postLoadMethods)
            {
                PostLoadMethodLifecycleExtension extension = BeanContext.RegisterAnonymousBean<PostLoadMethodLifecycleExtension>()
                        .PropertyValue("Method", postLoadMethod).Finish();
                allExtensions.Add(extension);
            }
            ((EntityMetaData)entityMetaData).EntityLifecycleExtensions = allExtensions.ToArray();
        }

        protected void UpdateAllEntityMetaDataWithLifecycleExtensions()
        {
            ILinkedMap<Type, IEntityMetaData> typeToMetaDataMap = GetExtensions();
            foreach (Entry<Type, IEntityMetaData> entry in typeToMetaDataMap)
            {
                UpdateEntityMetaDataWithLifecycleExtensions(entry.Value);
            }
        }

        protected void FillMethodsAnnotatedWith(Type type, IList<MethodInfo> methods, params Type[] annotations)
        {
            if (type == null || typeof(Object).Equals(type))
            {
                return;
            }
            FillMethodsAnnotatedWith(type.BaseType, methods, annotations);
            MethodInfo[] allMethodsOfThisType = type.GetMethods(BindingFlags.DeclaredOnly | BindingFlags.Public | BindingFlags.NonPublic);
            for (int a = 0, size = allMethodsOfThisType.Length; a < size; a++)
            {
                MethodInfo currentMethod = allMethodsOfThisType[a];
                for (int b = annotations.Length; b-- > 0; )
                {
                    if (AnnotationUtil.GetAnnotation(annotations[b], currentMethod, false) == null)
                    {
                        continue;
                    }
                    if (currentMethod.GetParameters().Length != 0)
                    {
                        throw new Exception("It is not allowed to annotated methods without " + annotations[b].FullName + " having 0 arguments: "
                                + currentMethod.ToString());
                    }
                    methods.Add(currentMethod);
                }
            }
        }

        protected void InitializeValueObjectMapping()
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
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

        public Type[] GetEntityPersistOrder()
        {
            return businessObjectSaveOrder;
        }
    }
}