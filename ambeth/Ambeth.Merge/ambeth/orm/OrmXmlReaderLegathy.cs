using System;
using System.Collections.Generic;
using System.Xml.Linq;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;

namespace De.Osthus.Ambeth.Orm
{
    public class OrmXmlReaderLegathy : IOrmXmlReader
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Property(ServiceConfigurationConstants.IndependentMetaData, DefaultValue = "false")]
        public virtual bool IndependentMetaData { protected get; set; }

        [Autowired]
        public IProxyHelper ProxyHelper { protected get; set; }

        [Autowired]
        public IXmlConfigUtil XmlConfigUtil { protected get; set; }

        public ISet<EntityConfig> LoadFromDocument(XDocument doc)
        {
            ISet<EntityConfig> entities = new CHashSet<EntityConfig>();
            LoadFromDocument(doc, entities, entities);
            return entities;
        }

        public void LoadFromDocument(XDocument doc, ISet<EntityConfig> localEntities, ISet<EntityConfig> externalEntities)
        {
            List<XElement> entityNodes = new List<XElement>();
            IMap<String,IList<XElement>> childrenMap = XmlConfigUtil.ChildrenToElementMap(doc.Root);
            if (childrenMap.ContainsKey(XmlConstants.ENTITY.LocalName))
            {
                entityNodes.AddRange(childrenMap.Get(XmlConstants.ENTITY.LocalName));
            }

            for (int i = entityNodes.Count; i-- > 0; )
            {
                XElement entityNode = entityNodes[i];
                EntityConfig entityConfig = ReadEntityConfig(entityNode);
                if (localEntities.Contains(entityConfig) || externalEntities.Contains(entityConfig))
                {
                    throw new Exception("Duplicate orm configuration for entity '" + entityConfig.EntityType.Name + "'");
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

        protected EntityConfig ReadEntityConfig(XElement entityTag)
        {
            String entityTypeName = XmlConfigUtil.GetRequiredAttribute(entityTag, XmlConstants.CLASS);
            try
            {
                Type entityType = XmlConfigUtil.GetTypeForName(entityTypeName);
                Type realType = ProxyHelper.GetRealType(entityType);
                EntityConfig entityConfig = new EntityConfig(entityType, realType);

                bool localEntity = !XmlConfigUtil.GetAttribute(entityTag, XmlConstants.TYPE).Equals(XmlConstants.EXTERN.LocalName);
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
                if (entityDefs.ContainsKey(XmlConstants.ATTR.LocalName))
                {
                    attributeMap = XmlConfigUtil.ChildrenToElementMap(entityDefs.Get(XmlConstants.ATTR.LocalName)[0]);
                }
                bool versionRequired = true;
                if (attributeMap != null)
                {
                    if (attributeMap.ContainsKey(XmlConstants.ID.LocalName))
                    {
                        XElement idElement = attributeMap.Get(XmlConstants.ID.LocalName)[0];
                        MemberConfig idMemberConfig = ReadMemberConfig(idElement);
                        entityConfig.IdMemberConfig = idMemberConfig;
                    }
                    else if (!localEntity)
                    {
                        throw new Exception("ID member name has to be set on external entities");
                    }

                    if (attributeMap.ContainsKey(XmlConstants.VERSION.LocalName))
                    {
                        XElement versionElement = attributeMap.Get(XmlConstants.VERSION.LocalName)[0];
                        versionRequired = XmlConfigUtil.AttributeIsTrue(versionElement, XmlConstants.WITHOUT);
                        if (versionRequired)
                        {
                            MemberConfig versionMemberConfig = ReadMemberConfig(versionElement);
                            entityConfig.VersionMemberConfig = versionMemberConfig;
                        }
                    }
                    else if (!localEntity)
                    {
                        throw new Exception("Version member name has to be set on external entities");
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

                    if (attributeMap.ContainsKey(XmlConstants.TO_ONE.LocalName))
                    {
                        IList<XElement> toOneAttrs = attributeMap.Get(XmlConstants.TO_ONE.LocalName);
                        for (int j = toOneAttrs.Count; j-- > 0; )
                        {
                            XElement toOneElement = toOneAttrs[j];
                            RelationConfigLegathy relationConfig = ReadRelationConfig(toOneElement, localEntity, true);
                            entityConfig.AddRelationConfig(relationConfig);
                        }
                    }

                    if (attributeMap.ContainsKey(XmlConstants.TO_MANY.LocalName))
                    {
                        IList<XElement> toManyAttrs = attributeMap.Get(XmlConstants.TO_MANY.LocalName);
                        for (int j = toManyAttrs.Count; j-- > 0; )
                        {
                            XElement toManyElement = toManyAttrs[j];
                            RelationConfigLegathy relationConfig = ReadRelationConfig(toManyElement, localEntity, false);
                            entityConfig.AddRelationConfig(relationConfig);
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
                }
                entityConfig.VersionRequired = versionRequired;

                return entityConfig;
            }
            catch (Exception e)
            {
                throw new Exception("Error occured while processing mapping for entity: " + entityTypeName, e);
            }
        }

        protected MemberConfig ReadMemberConfig(XElement memberElement)
        {
            String memberName = XmlConfigUtil.GetRequiredAttribute(memberElement, XmlConstants.NAME, true);
            String columnName = null;
            XElement columnElement = XmlConfigUtil.GetChildUnique(memberElement, XmlConstants.COLUMN);
            if (columnElement != null)
            {
                columnName = XmlConfigUtil.GetRequiredAttribute(columnElement, XmlConstants.NAME);
            }
            MemberConfig memberConfig = new MemberConfig(memberName, columnName);

            bool alternateId = XmlConfigUtil.AttributeIsTrue(memberElement, XmlConstants.ALT_ID);
            memberConfig.AlternateId = alternateId;

            return memberConfig;
        }

        protected RelationConfigLegathy ReadRelationConfig(XElement relationElement, bool localEntity, bool toOne)
        {
            String relationName = XmlConfigUtil.GetRequiredAttribute(relationElement, XmlConstants.NAME, true);
            try
            {
                RelationConfigLegathy relationConfig = new RelationConfigLegathy(relationName, toOne);

                String linkedEntityName = XmlConfigUtil.GetRequiredAttribute(relationElement, XmlConstants.TARGET_ENTITY);
                Type linkedEntityType = XmlConfigUtil.GetTypeForName(linkedEntityName);
                relationConfig.LinkedEntityType = linkedEntityType;

                bool doDelete = XmlConfigUtil.AttributeIsTrue(relationElement, XmlConstants.DO_DELETE);
                relationConfig.DoDelete = doDelete;
                bool mayDelete = XmlConfigUtil.AttributeIsTrue(relationElement, XmlConstants.MAY_DELETE);
                relationConfig.MayDelete = mayDelete;

                if (localEntity)
                {
                    XElement joinTableTag = XmlConfigUtil.GetChildUnique(relationElement, XmlConstants.JOIN_TABLE);
                    if (joinTableTag == null)
                    {
                        String constraintName = XmlConfigUtil.GetAttribute(relationElement, XmlConstants.CONSTRAINT_NAME);
                        if (constraintName.Length == 0 && !IndependentMetaData)
                        {
                            throw new ArgumentException("Either nested element '" + XmlConstants.JOIN_TABLE + "' or attribute '"
                                    + XmlConstants.CONSTRAINT_NAME + "' required to map link");
                        }
                        relationConfig.ConstraintName = constraintName;
                    }
                    else
                    {
                        String joinTableName = XmlConfigUtil.GetRequiredAttribute(joinTableTag, XmlConstants.NAME);
                        relationConfig.JoinTableName = joinTableName;

                        String fromFieldName = XmlConfigUtil.GetChildElementAttribute(joinTableTag, XmlConstants.JOIN_COLUMN, XmlConstants.NAME,
                                "Join column name has to be set exactly once");
                        relationConfig.FromFieldName = fromFieldName;
                        String toFieldName = XmlConfigUtil.GetChildElementAttribute(joinTableTag, XmlConstants.INV_JOIN_COLUMN, XmlConstants.NAME, null);
                        relationConfig.ToFieldName = toFieldName;

                        String toAttributeName = XmlConfigUtil.GetChildElementAttribute(joinTableTag, XmlConstants.INV_JOIN_ATTR, XmlConstants.NAME, null);
                        toAttributeName = StringConversionHelper.UpperCaseFirst(toAttributeName);
                        relationConfig.ToAttributeName = toAttributeName;

                        if (toFieldName == null && toAttributeName == null)
                        {
                            throw new ArgumentException("Inverse join column or attribute name has to be set");
                        }
                    }
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
