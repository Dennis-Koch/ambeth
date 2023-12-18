package com.koch.ambeth.merge.orm;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.io.FileUtil;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.xml.IXmlConfigUtil;
import com.koch.ambeth.util.xml.IXmlValidator;
import com.koch.ambeth.util.xml.XmlConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrmXmlReader20 implements IOrmXmlReader, IInitializingBean {
    public static final String ORM_XML_NS = "http://schema.kochdev.com/ambeth/ambeth_orm_2_0";

    private static final String[] XSD_FILE_NAMES = {
            "com/koch/ambeth/schema/ambeth_simple_types_2_0.xsd", "com/koch/ambeth/schema/ambeth_orm_2_0.xsd"
    };
    @Autowired
    protected IXmlConfigUtil xmlConfigUtil;
    @Autowired
    protected IProxyHelper proxyHelper;
    protected IXmlValidator validator;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        var rollback = FileUtil.pushCurrentTypeScope(getClass());
        try {
            validator = xmlConfigUtil.createValidator(XSD_FILE_NAMES);
        } finally {
            rollback.rollback();
        }
    }

    @Override
    public Set<EntityConfig> loadFromDocument(Document doc, IOrmEntityTypeProvider ormEntityTypeProvider) {
        var entities = new HashSet<EntityConfig>();
        loadFromDocument(doc, entities, entities, ormEntityTypeProvider);
        return entities;
    }

    @Override
    public void loadFromDocument(Document doc, Set<EntityConfig> localEntities, Set<EntityConfig> externalEntities, IOrmEntityTypeProvider ormEntityTypeProvider) {
        validateDocument(doc);

        var nameToLinkMap = new HashMap<String, ILinkConfig>();

        var links = new ArrayList<ILinkConfig>();
        var linkElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.LINK));
        var eLinkElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.EXTERNAL_LINK));
        var iLinkElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.INDEPENDENT_LINK));
        for (int i = linkElements.size(); i-- > 0; ) {
            var linkTag = linkElements.get(i);
            var link = readLinkConfig(linkTag);
            links.add(link);
        }
        for (int i = eLinkElements.size(); i-- > 0; ) {
            var linkTag = eLinkElements.get(i);
            var link = readExternalLinkConfig(linkTag);
            links.add(link);
        }
        for (int i = iLinkElements.size(); i-- > 0; ) {
            var linkTag = iLinkElements.get(i);
            var link = readIndependentLinkConfig(linkTag);
            links.add(link);
        }
        for (int i = links.size(); i-- > 0; ) {
            ILinkConfig link = links.get(i);

            if (link.getSource() != null) {
                if (nameToLinkMap.put(link.getSource(), link) != null) {
                    throw new IllegalStateException("Duplicate orm configuration for link '" + link.getSource() + "'");
                }
            }
            if (link.getAlias() != null) {
                if (nameToLinkMap.put(link.getAlias(), link) != null) {
                    throw new IllegalStateException("Duplicate orm configuration for link '" + link.getAlias() + "'");
                }
            }
        }

        var entityElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.ENTITY));
        var externalEntityElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.EXTERNAL_ENTITY));
        entityElements.addAll(externalEntityElements);
        for (int i = entityElements.size(); i-- > 0; ) {
            var entityTag = entityElements.get(i);
            var entityConfig = readEntityConfig(entityTag, nameToLinkMap, ormEntityTypeProvider);
            if (localEntities.contains(entityConfig) || externalEntities.contains(entityConfig)) {
                throw new IllegalStateException("Duplicate orm configuration for entity '" + entityConfig.getEntityType().getName() + "'");
            }
            if (entityConfig.isLocal()) {
                localEntities.add(entityConfig);
            } else {
                externalEntities.add(entityConfig);
            }
        }
    }

    protected void validateDocument(Document doc) {
        try {
            validator.validate(doc);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e, "Error during xml document validation");
        }
    }

    protected LinkConfig readLinkConfig(Element linkTag) {
        var source = xmlConfigUtil.getRequiredAttribute(linkTag, XmlConstants.SOURCE);
        var link = new LinkConfig(source);

        var cascadeDeleteRaw = xmlConfigUtil.getAttribute(linkTag, XmlConstants.CASCADE_DELETE);
        if (!cascadeDeleteRaw.isEmpty()) {
            var cascadeDelete = CascadeDeleteDirection.valueOf(cascadeDeleteRaw.toUpperCase());
            link.setCascadeDeleteDirection(cascadeDelete);
        }

        var alias = xmlConfigUtil.getAttribute(linkTag, XmlConstants.ALIAS);
        if (!alias.isEmpty()) {
            link.setAlias(alias);
        }

        return link;
    }

    protected ExternalLinkConfig readExternalLinkConfig(Element linkTag) {
        var link = readLinkConfig(linkTag);
        var eLink = new ExternalLinkConfig(link.getSource());

        var sourceColumn = xmlConfigUtil.getRequiredAttribute(linkTag, XmlConstants.SOURCE_COLUMN);
        eLink.setSourceColumn(sourceColumn);
        var targetMember = xmlConfigUtil.getRequiredAttribute(linkTag, XmlConstants.TARGET_MEMBER);
        eLink.setTargetMember(targetMember);

        eLink.setCascadeDeleteDirection(link.getCascadeDeleteDirection());
        eLink.setAlias(link.getAlias());

        return eLink;
    }

    protected ILinkConfig readIndependentLinkConfig(Element linkTag) {
        var alias = xmlConfigUtil.getRequiredAttribute(linkTag, XmlConstants.ALIAS);
        var link = new IndependentLinkConfig(alias);

        var cascadeDeleteRaw = xmlConfigUtil.getAttribute(linkTag, XmlConstants.CASCADE_DELETE);
        if (!cascadeDeleteRaw.isEmpty()) {
            var cascadeDelete = CascadeDeleteDirection.valueOf(cascadeDeleteRaw.toUpperCase());
            link.setCascadeDeleteDirection(cascadeDelete);
        }

        var leftStr = xmlConfigUtil.getAttribute(linkTag, XmlConstants.LEFT);
        if (!leftStr.isEmpty()) {
            var left = xmlConfigUtil.getTypeForName(leftStr);
            link.setLeft(left);
        }

        var rightStr = xmlConfigUtil.getAttribute(linkTag, XmlConstants.RIGHT);
        if (!rightStr.isEmpty()) {
            var right = xmlConfigUtil.getTypeForName(rightStr);
            link.setRight(right);
        }

        return link;
    }

    protected EntityConfig readEntityConfig(Element entityTag, Map<String, ILinkConfig> nameToLinkMap, IOrmEntityTypeProvider ormEntityTypeProvider) {
        var entityTypeName = xmlConfigUtil.getRequiredAttribute(entityTag, XmlConstants.CLASS);
        try {
            var entityType = ormEntityTypeProvider.resolveEntityType(entityTypeName);
            var realType = proxyHelper.getRealType(entityType);
            var entityConfig = new EntityConfig(entityType, realType);

            var localEntity = !entityTag.getNodeName().equals(XmlConstants.EXTERNAL_ENTITY);
            entityConfig.setLocal(localEntity);

            IMap<String, List<Element>> attributeMap = null;

            var entityDefs = xmlConfigUtil.childrenToElementMap(entityTag);
            if (entityDefs.containsKey(XmlConstants.TABLE)) {
                var specifiedTableName = xmlConfigUtil.getRequiredAttribute(entityDefs.get(XmlConstants.TABLE).get(0), XmlConstants.NAME);
                entityConfig.setTableName(specifiedTableName);
            }
            if (entityDefs.containsKey(XmlConstants.PERMISSION_GROUP)) {
                var permissionGroupName = xmlConfigUtil.getRequiredAttribute(entityDefs.get(XmlConstants.PERMISSION_GROUP).get(0), XmlConstants.NAME);
                entityConfig.setPermissionGroupName(permissionGroupName);
            }
            if (entityDefs.containsKey(XmlConstants.SEQ)) {
                var sequenceName = xmlConfigUtil.getRequiredAttribute(entityDefs.get(XmlConstants.SEQ).get(0), XmlConstants.NAME);
                entityConfig.setSequenceName(sequenceName);
            }
            if (entityDefs.containsKey(XmlConstants.DESCRIMINATOR)) {
                var descriminatorName = xmlConfigUtil.getRequiredAttribute(entityDefs.get(XmlConstants.DESCRIMINATOR).get(0), XmlConstants.NAME);
                entityConfig.setDescriminatorName(descriminatorName);
            }

            if (entityDefs.containsKey(XmlConstants.ATTR)) {
                attributeMap = xmlConfigUtil.toElementMap(entityDefs.get(XmlConstants.ATTR).get(0).getChildNodes());
            }
            var versionRequired = true;
            if (attributeMap != null) {
                var allIdMemberConfigs = new HashMap<String, MemberConfig>();
                if (attributeMap.containsKey(XmlConstants.ID)) {
                    var idMemberConfig = readUniqueMemberConfig(XmlConstants.ID, attributeMap);
                    entityConfig.setIdMemberConfig(idMemberConfig);
                    allIdMemberConfigs.put(idMemberConfig.getName(), idMemberConfig);
                } else if (attributeMap.containsKey(XmlConstants.ID_COMP)) {
                    var memberElement = attributeMap.get(XmlConstants.ID_COMP).get(0);
                    var idMemberConfig = readCompositeMemberConfig(memberElement, allIdMemberConfigs);
                    entityConfig.setIdMemberConfig(idMemberConfig);
                } else if (!localEntity) {
                    throw new IllegalArgumentException("ID member name has to be set on external entities");
                }

                if (attributeMap.containsKey(XmlConstants.ALT_ID)) {
                    var altIds = attributeMap.get(XmlConstants.ALT_ID);
                    for (int j = altIds.size(); j-- > 0; ) {
                        var memberElement = altIds.get(j);
                        var memberConfig = readMemberConfig(memberElement);
                        memberConfig.setAlternateId(true);
                        entityConfig.addMemberConfig(memberConfig);
                        allIdMemberConfigs.put(memberConfig.getName(), memberConfig);
                    }
                }

                if (attributeMap.containsKey(XmlConstants.ALT_ID_COMP)) {
                    var altIdsComp = attributeMap.get(XmlConstants.ALT_ID_COMP);
                    for (int j = altIdsComp.size(); j-- > 0; ) {
                        var memberElement = altIdsComp.get(j);
                        var memberConfig = readCompositeMemberConfig(memberElement, allIdMemberConfigs);
                        memberConfig.setAlternateId(true);
                        entityConfig.addMemberConfig(memberConfig);
                    }
                }

                if (attributeMap.containsKey(XmlConstants.VERSION)) {
                    var versionMemberConfig = readUniqueMemberConfig(XmlConstants.VERSION, attributeMap);
                    entityConfig.setVersionMemberConfig(versionMemberConfig);
                } else if (attributeMap.containsKey(XmlConstants.NO_VERSION)) {
                    versionRequired = false;
                } else if (!localEntity) {
                    throw new IllegalArgumentException("Version member name has to be set on external entities");
                }

                if (attributeMap.containsKey(XmlConstants.CREATED_BY)) {
                    var createdByMemberConfig = readUniqueMemberConfig(XmlConstants.CREATED_BY, attributeMap);
                    entityConfig.setCreatedByMemberConfig(createdByMemberConfig);
                }
                if (attributeMap.containsKey(XmlConstants.CREATED_ON)) {
                    var createdOnMemberConfig = readUniqueMemberConfig(XmlConstants.CREATED_ON, attributeMap);
                    entityConfig.setCreatedOnMemberConfig(createdOnMemberConfig);
                }
                if (attributeMap.containsKey(XmlConstants.UPDATED_BY)) {
                    var updatedByMemberConfig = readUniqueMemberConfig(XmlConstants.UPDATED_BY, attributeMap);
                    entityConfig.setUpdatedByMemberConfig(updatedByMemberConfig);
                }
                if (attributeMap.containsKey(XmlConstants.UPDATED_ON)) {
                    var updatedOnMemberConfig = readUniqueMemberConfig(XmlConstants.UPDATED_ON, attributeMap);
                    entityConfig.setUpdatedOnMemberConfig(updatedOnMemberConfig);
                }

                if (attributeMap.containsKey(XmlConstants.BASIC)) {
                    var basicAttrs = attributeMap.get(XmlConstants.BASIC);
                    for (int j = basicAttrs.size(); j-- > 0; ) {
                        var memberElement = basicAttrs.get(j);
                        var memberConfig = readMemberConfig(memberElement);
                        entityConfig.addMemberConfig(memberConfig);
                    }
                }

                if (attributeMap.containsKey(XmlConstants.IGNORE)) {
                    var ignoreAttrs = attributeMap.get(XmlConstants.IGNORE);
                    for (int j = ignoreAttrs.size(); j-- > 0; ) {
                        var ignoreElement = ignoreAttrs.get(j);
                        var memberConfig = readMemberConfig(ignoreElement);
                        memberConfig.setIgnore(true);
                        entityConfig.addMemberConfig(memberConfig);
                    }
                }

                if (attributeMap.containsKey(XmlConstants.RELATION)) {
                    var relationAttrs = attributeMap.get(XmlConstants.RELATION);
                    for (int j = relationAttrs.size(); j-- > 0; ) {
                        var relationElement = relationAttrs.get(j);
                        var relationConfig = readRelationConfig(relationElement, nameToLinkMap);
                        entityConfig.addRelationConfig(relationConfig);
                    }
                }
            }
            entityConfig.setVersionRequired(versionRequired);

            return entityConfig;
        } catch (RuntimeException e) {
            throw RuntimeExceptionUtil.mask(e, "Error occured while processing mapping for entity: " + entityTypeName);
        }
    }

    protected MemberConfig readUniqueMemberConfig(String tagName, IMap<String, List<Element>> attributeMap) {
        var memberElement = attributeMap.get(tagName).get(0);
        var memberConfig = readMemberConfig(memberElement);
        return memberConfig;
    }

    protected MemberConfig readMemberConfig(Element memberElement) {
        var memberName = xmlConfigUtil.getRequiredAttribute(memberElement, XmlConstants.NAME);
        var memberConfig = new MemberConfig(memberName);

        var columnName = xmlConfigUtil.getAttribute(memberElement, XmlConstants.COLUMN);
        if (!columnName.isEmpty()) {
            memberConfig.setColumnName(columnName);
        }
        var transientValue = xmlConfigUtil.getAttribute(memberElement, XmlConstants.TRANSIENT);
        if (!transientValue.isEmpty()) {
            memberConfig.setTransient(Boolean.parseBoolean(transientValue));
        }
        var definedByValue = xmlConfigUtil.getAttribute(memberElement, XmlConstants.DEFINED_BY);
        if (!definedByValue.isEmpty()) {
            memberConfig.setDefinedBy(definedByValue);
        }
        return memberConfig;
    }

    protected CompositeMemberConfig readCompositeMemberConfig(Element memberElement, IMap<String, MemberConfig> allIdMemberConfigs) {
        var idFragmentNodes = memberElement.getElementsByTagName(XmlConstants.ID_FRAGMENT);
        var idFragments = xmlConfigUtil.nodesToElements(idFragmentNodes);
        var memberConfigs = new MemberConfig[idFragments.size()];
        for (int i = 0; i < idFragments.size(); i++) {
            var idFragment = idFragments.get(i);
            var memberName = xmlConfigUtil.getRequiredAttribute(idFragment, XmlConstants.NAME);
            var memberConfig = allIdMemberConfigs.get(memberName);
            if (memberConfig == null) {
                memberConfig = readMemberConfig(idFragment);
                allIdMemberConfigs.put(memberName, memberConfig);
            }
            memberConfigs[i] = memberConfig;
        }
        var compositeMemberConfig = new CompositeMemberConfig(memberConfigs);
        return compositeMemberConfig;
    }

    protected IRelationConfig readRelationConfig(Element relationElement, Map<String, ILinkConfig> nameToLinkMap) {
        var relationName = xmlConfigUtil.getRequiredAttribute(relationElement, XmlConstants.NAME);
        var linkName = xmlConfigUtil.getAttribute(relationElement, XmlConstants.LINK);
        ILinkConfig linkConfig = null;
        if (!linkName.isEmpty()) {
            linkConfig = nameToLinkMap.get(linkName);
        }
        if (linkConfig == null) {
            if (log.isInfoEnabled()) {
                if (!linkName.isEmpty()) {
                    log.info("No LinkConfig found for name '" + linkName + "'. Creating one with default values.");
                } else {
                    log.info("Unconfigured Link found for property '" + relationName + "'. Trying to resolve this later.");
                }
            }
            linkConfig = new LinkConfig(linkName);
        }
        try {
            var relationConfig = new RelationConfig20(relationName, linkConfig);

            var entityIdentifierName = xmlConfigUtil.getAttribute(relationElement, XmlConstants.THIS);
            if (!entityIdentifierName.isEmpty()) {
                EntityIdentifier entityIdentifier = EntityIdentifier.valueOf(entityIdentifierName.toUpperCase());
                relationConfig.setEntityIdentifier(entityIdentifier);
            }

            return relationConfig;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error occured while processing relation '" + relationName + "'", e);
        }
    }
}
