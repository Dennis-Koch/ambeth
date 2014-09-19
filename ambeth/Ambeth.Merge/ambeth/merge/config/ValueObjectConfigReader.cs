using System;
using System.Collections.Generic;
using System.Xml.Linq;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;

namespace De.Osthus.Ambeth.Merge.Config
{
    public class ValueObjectConfigReader : IEventListener, IDisposableBean, IInitializingBean, IStartingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        private static readonly XName[] memberTagNames = { XmlConstants.BASIC, XmlConstants.RELATION };

        protected ISet<IValueObjectConfig> managedValueObjectConfigs = new LinkedHashSet<IValueObjectConfig>();

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        [Autowired]
        public IValueObjectConfigExtendable ValueObjectConfigExtendable { protected get; set; }

        [Autowired]
        public IXmlConfigUtil XmlConfigUtil { protected get; set; }

        [Property(MergeConfigurationConstants.ValueObjectConfigValidationActive, DefaultValue = "false")]
        public bool RuntimeValidationActive { protected get; set; }

        protected String xmlFileName = null;

        protected HashMap<Type, IList<XElement>> configsToConsume;

        public void AfterPropertiesSet()
	    {
		    if (xmlFileName != null)
		    {
			    XDocument[] docs = XmlConfigUtil.ReadXmlFiles(xmlFileName);
			    ParamChecker.AssertNotNull(docs, "docs");
			    configsToConsume = ReadConfig(docs);
		    }
	    }

	    public void AfterStarted()
        {
		    if (configsToConsume == null)
		    {
			    return;
		    }
		    foreach (Entry<Type, IList<XElement>> entry in configsToConsume)
		    {
			    Type entityType = entry.Key;
			    IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType, true);
			    if (metaData == null)
			    {
				    if (Log.InfoEnabled)
				    {
					    Log.Info("Could not resolve entity meta data for '" + entityType.Name + "'");
				    }
			    }
		    }
	    }

        public void Destroy()
        {
            foreach (IValueObjectConfig config in managedValueObjectConfigs)
            {
                ValueObjectConfigExtendable.UnregisterValueObjectConfig(config);
            }
        }

        [Property(ServiceConfigurationConstants.ValueObjectFile, Mandatory = false)]
        public String FileName
        {
            set
            {
                if (xmlFileName != null)
                {
                    throw new ArgumentException("ValueObjectConfigReader already configured! Tried to set the config file: " + value
                            + "'. File name is already set to '" + xmlFileName + "'");
                }

                xmlFileName = value;
            }
        }

        public void HandleEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
        {
            if (!(eventObject is EntityMetaDataAddedEvent))
            {
                return;
            }
            if (configsToConsume == null)
		    {
			    return;
		    }
		    foreach (Type entityType in ((EntityMetaDataAddedEvent) eventObject).EntityTypes)
		    {
			    IList<XElement> configs = configsToConsume.Get(entityType);
			    if (configs == null)
			    {
				    continue;
			    }
			    IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);
			    ConsumeConfigs(metaData, configs);
		    }
        }

        protected HashMap<Type, IList<XElement>> ReadConfig(XDocument[] docs)
	    {
            HashMap<Type, IList<XElement>> entities = new HashMap<Type, IList<XElement>>();
            foreach (XDocument doc in docs)
            {
                IList<XElement> docEntities = new List<XElement>(doc.Descendants(XmlConstants.ENTITY));
                for (int a = docEntities.Count; a-- > 0;)
			    {
				    XElement docEntity = docEntities[a];
				    Type entityType = ResolveEntityType(docEntity);
				    if (entityType == null)
				    {
					    // ignore all entries without a valid entity type mapping
					    continue;
				    }
				    IList<XElement> list = entities.Get(entityType);
				    if (list == null)
				    {
					    list = new List<XElement>();
					    entities.Put(entityType, list);
				    }
				    list.Add(docEntity);
			    }
		    }
		    return entities;
        }

        protected Type ResolveEntityType(XElement item)
	    {
		    IMap<String, IList<XElement>> configs = XmlConfigUtil.ChildrenToElementMap(item);
		    if (!configs.ContainsKey(XmlConstants.VALUE_OBJECT.LocalName))
		    {
			    return null;
		    }
		    String entityTypeName = XmlConfigUtil.GetRequiredAttribute(item, XmlConstants.CLASS);
		    return XmlConfigUtil.GetTypeForName(entityTypeName);
	    }

        protected void ConsumeConfigs(IEntityMetaData metaData, IList<XElement> entities)
        {
            for (int i = entities.Count; i-- > 0; )
            {
                XElement item = entities[i];

                IMap<String, IList<XElement>> configs = XmlConfigUtil.ChildrenToElementMap(item);
                IList<XElement> voConfigs = configs.Get(XmlConstants.VALUE_OBJECT.LocalName);
                for (int j = voConfigs.Count; j-- > 0; )
                {
                    XElement voConfig = voConfigs[j];

                    String valueTypeName = XmlConfigUtil.GetRequiredAttribute(voConfig, XmlConstants.CLASS);
                    Type valueType = XmlConfigUtil.GetTypeForName(valueTypeName);

                    bool exists = false;
                    foreach (IValueObjectConfig conf in managedValueObjectConfigs)
                    {
                        if (conf.ValueType.Equals(valueType) && conf.EntityType.Equals(metaData.EntityType))
                        {
                            exists = true;
                            break;
                        }
                    }
                    if (exists)
                    {
                        continue;
                    }

                    ValueObjectConfig config = new ValueObjectConfig();
                    config.EntityType = metaData.EntityType;
                    config.ValueType = valueType;

                    HandleMembers(config, voConfig, metaData);

                    managedValueObjectConfigs.Add(config);
                    ValueObjectConfigExtendable.RegisterValueObjectConfig(config);
                }
            }
        }

        protected void HandleMembers(ValueObjectConfig config, XElement voConfig, IEntityMetaData metaData)
        {
            IMap<String, IList<XElement>> configDetails = XmlConfigUtil.ChildrenToElementMap(voConfig);
            HandleIgnoredMembers(config, configDetails);
            HandleMemberMappings(config, configDetails, metaData);
            HandleRelations(config, configDetails);
        }

        protected void HandleIgnoredMembers(ValueObjectConfig config, IMap<String, IList<XElement>> configDetails)
        {
            IList<XElement> memberTags = configDetails.Get(XmlConstants.IGNORE.LocalName);
            if (memberTags == null)
            {
                return;
            }

            for (int j = memberTags.Count; j-- > 0; )
            {
                XElement element = memberTags[j];
                String memberName = XmlConfigUtil.GetRequiredAttribute(element, XmlConstants.NAME);
                config.SetValueObjectMemberType(memberName, ValueObjectMemberType.IGNORE);
            }
        }

        protected void HandleMemberMappings(ValueObjectConfig config, IMap<String, IList<XElement>> configDetails, IEntityMetaData metaData)
        {
            Type entityType = config.EntityType;
            Type valueType = config.ValueType;

            IMap<String, IPropertyInfo> entityPropertyMap = PropertyInfoProvider.GetPropertyMap(entityType);
            IMap<String, IPropertyInfo> valuePropertyMap = PropertyInfoProvider.GetPropertyMap(valueType);

            for (int i = memberTagNames.Length; i-- > 0; )
            {
                String memberTagName = memberTagNames[i].LocalName;

                IList<XElement> memberTags = configDetails.Get(memberTagName);
                if (memberTags == null)
                {
                    continue;
                }

                for (int j = memberTags.Count; j-- > 0; )
                {
                    XElement element = memberTags[j];
                    String memberName = XmlConfigUtil.GetRequiredAttribute(element, XmlConstants.NAME);
                    if (config.IsIgnoredMember(memberName))
                    {
                        continue;
                    }
                    if (RuntimeValidationActive && !IsPropertyResolvable(valueType, valuePropertyMap, memberName, null))
                    {
                        throw new ArgumentException("Value type property '" + valueType.Name + "." + memberName + "' not found");
                    }

                    bool holdsListType = XmlConfigUtil.AttributeIsTrue(element, XmlConstants.LIST_TYPE);
                    if (holdsListType)
                    {
                        config.AddListTypeMember(memberName);
                    }

                    String entityMemberName = XmlConfigUtil.GetAttribute(element, XmlConstants.NAME_IN_ENTITY);
                    if (entityMemberName.Length == 0)
                    {
                        entityMemberName = memberName;
                    }
                    else
                    {
                        config.PutValueObjectMemberName(entityMemberName, memberName);
                    }
                    if (RuntimeValidationActive && !IsPropertyResolvable(entityType, entityPropertyMap, entityMemberName, metaData))
                    {
                        throw new ArgumentException("Entity type property '" + entityType.Name + "." + entityMemberName
                                + "' not found while configuring value type '" + valueType.Name + "'");
                    }
                }
            }
        }

        protected bool IsPropertyResolvable(Type type, IMap<String, IPropertyInfo> propertyMap, String memberName, IEntityMetaData metaData)
        {
            if (metaData != null && metaData.GetMemberByName(memberName) == null)
            {
                return false;
            }
            if (propertyMap == null)
            {
                propertyMap = PropertyInfoProvider.GetPropertyMap(type);
            }
            if (propertyMap.ContainsKey(memberName))
            {
                return true;
            }
            String[] memberPath = memberName.Split("\\.".ToCharArray());
            if (memberPath.Length == 0)
            {
                return false;
            }
            IPropertyInfo propertyInfo = propertyMap.Get(memberPath[0]);
            if (propertyInfo == null)
            {
                return false;
            }
            String remainingMemberName = memberName.Substring(memberPath[0].Length + 1);
            return IsPropertyResolvable(propertyInfo.PropertyType, null, remainingMemberName, metaData);
        }

        protected void HandlePrimitiveCollections(ValueObjectConfig config, IMap<String, IList<XElement>> configDetails)
        {
            IList<XElement> memberTags = configDetails.Get(XmlConstants.BASIC.LocalName);
            if (memberTags == null)
            {
                return;
            }
            for (int j = memberTags.Count; j-- > 0; )
            {
                XElement element = memberTags[j];
                String memberName = XmlConfigUtil.GetRequiredAttribute(element, XmlConstants.NAME);

                if (config.IsIgnoredMember(memberName))
                {
                    continue;
                }

                config.SetValueObjectMemberType(memberName, ValueObjectMemberType.BASIC);

                String targetElementTypeName = XmlConfigUtil.GetAttribute(element, XmlConstants.TARGET_ELEMENT_TYPE);
                if (targetElementTypeName.Length == 0)
                {
                    continue;
                }

                Type elementType = XmlConfigUtil.GetTypeForName(targetElementTypeName);
                config.PutMemberType(memberName, elementType);
            }
        }

        protected void HandleRelations(ValueObjectConfig config, IMap<String, IList<XElement>> configDetails)
        {
            IList<XElement> elementTypes = configDetails.Get(XmlConstants.RELATION.LocalName);
            if (elementTypes != null)
            {
                for (int i = elementTypes.Count; i-- > 0; )
                {
                    XElement entry = elementTypes[i];
                    String memberName = XmlConfigUtil.GetRequiredAttribute(entry, XmlConstants.NAME);

                    if (config.IsIgnoredMember(memberName))
                    {
                        continue;
                    }

                    config.SetValueObjectMemberType(memberName, ValueObjectMemberType.RELATION);

                    bool holdsListType = XmlConfigUtil.AttributeIsTrue(entry, XmlConstants.LIST_TYPE);
                    if (holdsListType)
                    {
                        config.AddListTypeMember(memberName);
                    }

                    String elementTypeName = XmlConfigUtil.GetAttribute(entry, XmlConstants.TARGET_VALUE_OBJECT);
                    if (elementTypeName.Length > 0)
                    {
                        Type elementType = XmlConfigUtil.GetTypeForName(elementTypeName);
                        config.PutMemberType(memberName, elementType);
                    }
                }
            }
        }
    }
}