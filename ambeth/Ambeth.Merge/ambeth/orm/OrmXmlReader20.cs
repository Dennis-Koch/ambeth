using System;
using System.Collections.Generic;
using System.Xml.Linq;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;
using De.Osthus.Ambeth.Exceptions;

namespace De.Osthus.Ambeth.Orm
{
    public class OrmXmlReader20 : IOrmXmlReader, IInitializingBean
    {
        public const String ORM_XML_NS = "http://osthus.de/ambeth/ambeth_orm_2_0";

        private static readonly String[] XSD_FILE_NAMES = { "ambeth/schema/ambeth_simple_types_2_0.xsd", "ambeth/schema/ambeth_orm_2_0.xsd" };

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IXmlConfigUtil XmlConfigUtil { protected get; set; }

        [Autowired]
        public IProxyHelper ProxyHelper { protected get; set; }

        protected IXmlValidator validator;

        public void AfterPropertiesSet()
        {
            validator = XmlConfigUtil.CreateValidator(XSD_FILE_NAMES);
        }

        public ISet<EntityConfig> LoadFromDocument(XDocument doc)
        {
            ISet<EntityConfig> entities = new HashSet<EntityConfig>();
            LoadFromDocument(doc, entities, entities);
            return entities;
        }

        public void LoadFromDocument(XDocument doc, ISet<EntityConfig> localEntities, ISet<EntityConfig> externalEntities)
        {
            ValidateDocument(doc);

            HashMap<String, ILinkConfig> nameToLinkMap = new HashMap<String, ILinkConfig>();

            List<ILinkConfig> links = new List<ILinkConfig>();
            IList<XElement> linkElements = GetDescendants(doc, XmlConstants.LINK);
            IList<XElement> eLinkElements =  GetDescendants(doc, XmlConstants.EXTERNAL_LINK);
            IList<XElement> iLinkElements =  GetDescendants(doc, XmlConstants.INDEPENDENT_LINK);
            for (int i = linkElements.Count; i-- > 0; )
            {
                XElement linkTag = linkElements[i];
                ILinkConfig link = ReadLinkConfig(linkTag);
                links.Add(link);
            }
            for (int i = eLinkElements.Count; i-- > 0; )
            {
                XElement linkTag = eLinkElements[i];
                ILinkConfig link = ReadExternalLinkConfig(linkTag);
                links.Add(link);
            }
            for (int i = iLinkElements.Count; i-- > 0; )
            {
                XElement linkTag = iLinkElements[i];
                ILinkConfig link = ReadIndependentLinkConfig(linkTag);
                links.Add(link);
            }
            for (int i = links.Count; i-- > 0; )
            {
                ILinkConfig link = links[i];

                if (link.Source != null)
                {
                    if (nameToLinkMap.Put(link.Source, link) != null)
                    {
                        throw new Exception("Duplicate orm configuration for link '" + link.Source + "'");
                    }
                }
                if (link.Alias != null)
                {
                    if (nameToLinkMap.Put(link.Alias, link) != null)
                    {
                        throw new Exception("Duplicate orm configuration for link '" + link.Alias + "'");
                    }
                }
            }

            List<XElement> entityElements = GetDescendants(doc, XmlConstants.ENTITY);
            IList<XElement> externalEntityElements = GetDescendants(doc, XmlConstants.EXTERNAL_ENTITY);
            entityElements.AddRange(externalEntityElements);
            for (int i = entityElements.Count; i-- > 0; )
            {
                XElement entityTag = entityElements[i];
                EntityConfig entityConfig = ReadEntityConfig(entityTag, nameToLinkMap);
                if (localEntities.Contains(entityConfig) || externalEntities.Contains(entityConfig))
                {
                    throw new Exception("Duplicate orm configuration for entity '" + entityConfig.EntityType.FullName + "'");
                }
                if (entityConfig.Local)
                {
                    localEntities.Add(entityConfig);
                }
                else
                {
                    externalEntities.Add(entityConfig);
                }
            }
        }

        protected List<XElement> GetDescendants(XContainer element, XName name)
        {
            return new List<XElement>(element.Descendants(XName.Get(name.LocalName, ORM_XML_NS)));
        }

        protected void ValidateDocument(XDocument doc)
        {
            try
            {
                validator.Validate(doc);
            }
            catch (Exception e)
            {
                throw RuntimeExceptionUtil.Mask(e, "Error during xml document validation");
            }
        }

        protected LinkConfig ReadLinkConfig(XElement linkTag)
        {
            String source = XmlConfigUtil.GetRequiredAttribute(linkTag, XmlConstants.SOURCE);
            LinkConfig link = new LinkConfig(source);

            String cascadeDeleteRaw = XmlConfigUtil.GetAttribute(linkTag, XmlConstants.CASCADE_DELETE);
            if (cascadeDeleteRaw.Length > 0)
            {
                CascadeDeleteDirection cascadeDelete = (CascadeDeleteDirection)Enum.Parse(typeof(CascadeDeleteDirection), cascadeDeleteRaw, true);
                link.CascadeDeleteDirection = cascadeDelete;
            }

            String alias = XmlConfigUtil.GetAttribute(linkTag, XmlConstants.ALIAS);
            if (alias.Length > 0)
            {
                link.Alias = alias;
            }

            return link;
        }

        protected ExternalLinkConfig ReadExternalLinkConfig(XElement linkTag)
        {
            LinkConfig link = ReadLinkConfig(linkTag);
            ExternalLinkConfig eLink = new ExternalLinkConfig(link.Source);

            String sourceColumn = XmlConfigUtil.GetRequiredAttribute(linkTag, XmlConstants.SOURCE_COLUMN);
            eLink.SourceColumn = sourceColumn;
            String targetMember = XmlConfigUtil.GetRequiredAttribute(linkTag, XmlConstants.TARGET_MEMBER);
            eLink.TargetMember = targetMember;

            eLink.CascadeDeleteDirection = link.CascadeDeleteDirection;
            eLink.Alias = link.Alias;

            return eLink;
        }

        protected ILinkConfig ReadIndependentLinkConfig(XElement linkTag)
        {
            String alias = XmlConfigUtil.GetRequiredAttribute(linkTag, XmlConstants.ALIAS);
            IndependentLinkConfig link = new IndependentLinkConfig(alias);

            String cascadeDeleteRaw = XmlConfigUtil.GetAttribute(linkTag, XmlConstants.CASCADE_DELETE);
            if (cascadeDeleteRaw.Length > 0)
            {
                CascadeDeleteDirection cascadeDelete = (CascadeDeleteDirection)Enum.Parse(typeof(CascadeDeleteDirection), cascadeDeleteRaw, true);
                link.CascadeDeleteDirection = cascadeDelete;
            }

            String leftStr = XmlConfigUtil.GetAttribute(linkTag, XmlConstants.LEFT);
            if (leftStr.Length > 0)
            {
                Type left = XmlConfigUtil.GetTypeForName(leftStr);
                link.Left = left;
            }

            String rightStr = XmlConfigUtil.GetAttribute(linkTag, XmlConstants.RIGHT);
            if (rightStr.Length > 0)
            {
                Type right = XmlConfigUtil.GetTypeForName(rightStr);
                link.Right = right;
            }

            return link;
        }

        protected EntityConfig ReadEntityConfig(XElement entityTag, IMap<String, ILinkConfig> nameToLinkMap)
        {
            String entityTypeName = XmlConfigUtil.GetRequiredAttribute(entityTag, XmlConstants.CLASS);
            try
            {
                Type entityType = XmlConfigUtil.GetTypeForName(entityTypeName);
                Type realType = ProxyHelper.GetRealType(entityType);
                EntityConfig entityConfig = new EntityConfig(entityType, realType);

                bool localEntity = !entityTag.Name.Equals(XmlConstants.EXTERNAL_ENTITY);
                entityConfig.Local = localEntity;

                IMap<String, IList<XElement>> attributeMap = null;

                IMap<String, IList<XElement>> entityDefs = XmlConfigUtil.ChildrenToElementMap(entityTag);
                if (entityDefs.ContainsKey(XmlConstants.TABLE.LocalName))
                {
                    String specifiedTableName = XmlConfigUtil.GetRequiredAttribute(entityDefs.Get(XmlConstants.TABLE.LocalName)[0], XmlConstants.NAME);
                    entityConfig.TableName = specifiedTableName;
                }
                if (entityDefs.ContainsKey(XmlConstants.PERMISSION_GROUP.LocalName))
                {
                    String permissionGroupName = XmlConfigUtil.GetRequiredAttribute(entityDefs.Get(XmlConstants.PERMISSION_GROUP.LocalName)[0], XmlConstants.NAME);
                    entityConfig.PermissionGroupName = permissionGroupName;
                }
                if (entityDefs.ContainsKey(XmlConstants.SEQ.LocalName))
                {
                    String sequenceName = XmlConfigUtil.GetRequiredAttribute(entityDefs.Get(XmlConstants.SEQ.LocalName)[0], XmlConstants.NAME);
                    entityConfig.SequenceName = sequenceName;
                }
				if (attributeMap.ContainsKey(XmlConstants.DESCRIMINATOR.LocalName))
				{
					String descriminatorName = XmlConfigUtil.GetRequiredAttribute(entityDefs.Get(XmlConstants.DESCRIMINATOR.LocalName)[0], XmlConstants.NAME);
					entityConfig.DescriminatorName = descriminatorName;
				}

                if (entityDefs.ContainsKey(XmlConstants.ATTR.LocalName))
                {
                    attributeMap = XmlConfigUtil.ToElementMap(entityDefs.Get(XmlConstants.ATTR.LocalName)[0].Elements());
                }
                bool versionRequired = true;
                if (attributeMap != null)
                {
                    IMap<String, MemberConfig> allIdMemberConfigs = new HashMap<String, MemberConfig>();
                    if (attributeMap.ContainsKey(XmlConstants.ID.LocalName))
                    {
                        MemberConfig idMemberConfig = ReadUniqueMemberConfig(XmlConstants.ID.LocalName, attributeMap);
                        entityConfig.IdMemberConfig = idMemberConfig;
                        allIdMemberConfigs.Put(idMemberConfig.Name, idMemberConfig);
                    }
                    else if (attributeMap.ContainsKey(XmlConstants.ID_COMP.LocalName))
                    {
                        XElement memberElement = attributeMap.Get(XmlConstants.ID_COMP.LocalName)[0];
                        IMemberConfig idMemberConfig = ReadCompositeMemberConfig(memberElement, allIdMemberConfigs);
                        entityConfig.IdMemberConfig = idMemberConfig;
                    }
                    else if (!localEntity)
                    {
                        throw new ArgumentException("ID member name has to be set on external entities");
                    }

                    if (attributeMap.ContainsKey(XmlConstants.ALT_ID.LocalName))
                    {
                        IList<XElement> altIds = attributeMap.Get(XmlConstants.ALT_ID.LocalName);
                        for (int j = altIds.Count; j-- > 0; )
                        {
                            XElement memberElement = altIds[j];
                            MemberConfig memberConfig = ReadMemberConfig(memberElement);
                            memberConfig.AlternateId = true;
                            entityConfig.AddMemberConfig(memberConfig);
                            allIdMemberConfigs.Put(memberConfig.Name, memberConfig);
                        }
                    }

                    if (attributeMap.ContainsKey(XmlConstants.ALT_ID_COMP.LocalName))
                    {
                        IList<XElement> altIdsComp = attributeMap.Get(XmlConstants.ALT_ID_COMP.LocalName);
                        for (int j = altIdsComp.Count; j-- > 0; )
                        {
                            XElement memberElement = altIdsComp[j];
                            CompositeMemberConfig memberConfig = ReadCompositeMemberConfig(memberElement, allIdMemberConfigs);
                            memberConfig.AlternateId = true;
                            entityConfig.AddMemberConfig(memberConfig);
                        }
                    }

                    if (attributeMap.ContainsKey(XmlConstants.VERSION.LocalName))
                    {
                        MemberConfig versionMemberConfig = ReadUniqueMemberConfig(XmlConstants.VERSION.LocalName, attributeMap);
                        entityConfig.VersionMemberConfig = versionMemberConfig;
                    }
                    else if (attributeMap.ContainsKey(XmlConstants.NO_VERSION.LocalName))
                    {
                        versionRequired = false;
                    }
                    else if (!localEntity)
                    {
                        throw new ArgumentException("Version member name has to be set on external entities");
                    }
                    if (attributeMap.ContainsKey(XmlConstants.CREATED_BY.LocalName))
                    {
                        MemberConfig createdByMemberConfig = ReadUniqueMemberConfig(XmlConstants.CREATED_BY.LocalName, attributeMap);
                        entityConfig.CreatedByMemberConfig = createdByMemberConfig;
                    }
                    if (attributeMap.ContainsKey(XmlConstants.CREATED_ON.LocalName))
                    {
                        MemberConfig createdOnMemberConfig = ReadUniqueMemberConfig(XmlConstants.CREATED_ON.LocalName, attributeMap);
                        entityConfig.CreatedOnMemberConfig = createdOnMemberConfig;
                    }
                    if (attributeMap.ContainsKey(XmlConstants.UPDATED_BY.LocalName))
                    {
                        MemberConfig updatedByMemberConfig = ReadUniqueMemberConfig(XmlConstants.UPDATED_BY.LocalName, attributeMap);
                        entityConfig.UpdatedByMemberConfig = updatedByMemberConfig;
                    }
                    if (attributeMap.ContainsKey(XmlConstants.UPDATED_ON.LocalName))
                    {
                        MemberConfig updatedOnMemberConfig = ReadUniqueMemberConfig(XmlConstants.UPDATED_ON.LocalName, attributeMap);
                        entityConfig.UpdatedOnMemberConfig = updatedOnMemberConfig;
                    }

                    if (attributeMap.ContainsKey(XmlConstants.BASIC.LocalName))
                    {
                        IList<XElement> basicAttrs = attributeMap.Get(XmlConstants.BASIC.LocalName);
                        for (int j = basicAttrs.Count; j-- > 0; )
                        {
                            XElement memberElement = basicAttrs[j];
                            MemberConfig memberConfig = ReadMemberConfig(memberElement);
                            entityConfig.AddMemberConfig(memberConfig);
                        }
                    }

                    if (attributeMap.ContainsKey(XmlConstants.IGNORE.LocalName))
                    {
                        IList<XElement> ignoreAttrs = attributeMap.Get(XmlConstants.IGNORE.LocalName);
                        for (int j = ignoreAttrs.Count; j-- > 0; )
                        {
                            XElement ignoreElement = ignoreAttrs[j];
                            MemberConfig memberConfig = ReadMemberConfig(ignoreElement);
                            memberConfig.Ignore = true;
                            entityConfig.AddMemberConfig(memberConfig);
                        }
                    }

                    if (attributeMap.ContainsKey(XmlConstants.RELATION.LocalName))
                    {
                        IList<XElement> relationAttrs = attributeMap.Get(XmlConstants.RELATION.LocalName);
                        for (int j = relationAttrs.Count; j-- > 0; )
                        {
                            XElement relationElement = relationAttrs[j];
                            IRelationConfig relationConfig = ReadRelationConfig(relationElement, nameToLinkMap);
                            entityConfig.AddRelationConfig(relationConfig);
                        }
                    }
                }
                entityConfig.VersionRequired = versionRequired;

                return entityConfig;
            }
            catch (Exception e)
            {
                throw RuntimeExceptionUtil.Mask(e, "Error occured while processing mapping for entity: " + entityTypeName);
            }
        }

        protected MemberConfig ReadUniqueMemberConfig(String tagName, IMap<String, IList<XElement>> attributeMap)
        {
            XElement memberElement = attributeMap.Get(tagName)[0];
            MemberConfig memberConfig = ReadMemberConfig(memberElement);
            return memberConfig;
        }

        protected MemberConfig ReadMemberConfig(XElement memberElement)
        {
            String memberName = XmlConfigUtil.GetRequiredAttribute(memberElement, XmlConstants.NAME);
            MemberConfig memberConfig = new MemberConfig(memberName);

            String columnName = XmlConfigUtil.GetAttribute(memberElement, XmlConstants.COLUMN);
            if (columnName.Length > 0)
            {
                memberConfig.ColumnName = columnName;
            }
			String transientValue = XmlConfigUtil.GetAttribute(memberElement, XmlConstants.TRANSIENT);
			if (transientValue.Length != 0)
			{
				memberConfig.Transient = Boolean.Parse(transientValue);
			}
			String definedByValue = XmlConfigUtil.GetAttribute(memberElement, XmlConstants.DEFINED_BY);
			if (definedByValue.Length != 0)
			{
				memberConfig.DefinedBy = definedByValue;
			}
            return memberConfig;
        }

        protected CompositeMemberConfig ReadCompositeMemberConfig(XElement memberElement, IMap<String, MemberConfig> allIdMemberConfigs)
        {
            IEnumerable<XElement> idFragmentNodes = GetDescendants(memberElement, XmlConstants.ID_FRAGMENT);
            IList<XElement> idFragments = XmlConfigUtil.NodesToElements(idFragmentNodes);
            MemberConfig[] memberConfigs = new MemberConfig[idFragments.Count];
            for (int i = 0; i < idFragments.Count; i++)
            {
                XElement idFragment = idFragments[i];
                String memberName = XmlConfigUtil.GetRequiredAttribute(idFragment, XmlConstants.NAME);
                MemberConfig memberConfig = allIdMemberConfigs.Get(memberName);
                if (memberConfig == null)
                {
                    memberConfig = ReadMemberConfig(idFragment);
                    allIdMemberConfigs.Put(memberName, memberConfig);
                }
                memberConfigs[i] = memberConfig;
            }
            CompositeMemberConfig compositeMemberConfig = new CompositeMemberConfig(memberConfigs);
            return compositeMemberConfig;
        }

        protected IRelationConfig ReadRelationConfig(XElement relationElement, IMap<String, ILinkConfig> nameToLinkMap)
        {
            String relationName = XmlConfigUtil.GetRequiredAttribute(relationElement, XmlConstants.NAME);
            String linkName = XmlConfigUtil.GetAttribute(relationElement, XmlConstants.LINK);
            ILinkConfig linkConfig = null;
            if (linkName.Length > 0)
            {
                linkConfig = nameToLinkMap.Get(linkName);
            }
            if (linkConfig == null)
            {
                if (Log.InfoEnabled)
                {
                    if (linkName.Length > 0)
                    {
                        Log.Info("No LinkConfig found for name '" + linkName + "'. Creating one with default values.");
                    }
                    else
                    {
                        Log.Info("Unconfigured Link found for property '" + relationName + "'. Trying to resolve this later.");
                    }
                }
                linkConfig = new LinkConfig(linkName);
            }
            try
            {
                RelationConfig20 relationConfig = new RelationConfig20(relationName, linkConfig);

                String entityIdentifierName = XmlConfigUtil.GetAttribute(relationElement, XmlConstants.THIS);
                if (entityIdentifierName.Length > 0)
                {
                    EntityIdentifier entityIdentifier = (EntityIdentifier)Enum.Parse(typeof(EntityIdentifier), entityIdentifierName, true);
                    relationConfig.EntityIdentifier = entityIdentifier;
                }

                return relationConfig;
            }
            catch (Exception e)
            {
                throw new Exception("Error occured while processing relation '" + relationName + "'", e);
            }
        }
    }
}