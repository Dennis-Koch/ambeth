package de.osthus.ambeth.orm;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlValidator;
import de.osthus.ambeth.util.xml.XmlConstants;

public class OrmXmlReader20 implements IOrmXmlReader, IInitializingBean
{
	public static final String ORM_XML_NS = "http://osthus.de/ambeth/ambeth_orm_2_0";

	private static final String[] XSD_FILE_NAMES = { "de/osthus/ambeth/schema/ambeth_simple_types_2_0.xsd", "de/osthus/ambeth/schema/ambeth_orm_2_0.xsd" };

	@LogInstance
	private ILogger log;

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	@Autowired
	protected IProxyHelper proxyHelper;

	protected IXmlValidator validator;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		validator = xmlConfigUtil.createValidator(XSD_FILE_NAMES);
	}

	@Override
	public Set<EntityConfig> loadFromDocument(Document doc)
	{
		ISet<EntityConfig> entities = new HashSet<EntityConfig>();
		loadFromDocument(doc, entities, entities);
		return entities;
	}

	@Override
	public void loadFromDocument(Document doc, Set<EntityConfig> localEntities, Set<EntityConfig> externalEntities)
	{
		validateDocument(doc);

		HashMap<String, ILinkConfig> nameToLinkMap = new HashMap<String, ILinkConfig>();

		ArrayList<ILinkConfig> links = new ArrayList<ILinkConfig>();
		IList<Element> linkElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.LINK));
		IList<Element> eLinkElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.EXTERNAL_LINK));
		IList<Element> iLinkElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.INDEPENDENT_LINK));
		for (int i = linkElements.size(); i-- > 0;)
		{
			Element linkTag = linkElements.get(i);
			ILinkConfig link = readLinkConfig(linkTag);
			links.add(link);
		}
		for (int i = eLinkElements.size(); i-- > 0;)
		{
			Element linkTag = eLinkElements.get(i);
			ILinkConfig link = readExternalLinkConfig(linkTag);
			links.add(link);
		}
		for (int i = iLinkElements.size(); i-- > 0;)
		{
			Element linkTag = iLinkElements.get(i);
			ILinkConfig link = readIndependentLinkConfig(linkTag);
			links.add(link);
		}
		for (int i = links.size(); i-- > 0;)
		{
			ILinkConfig link = links.get(i);

			if (link.getSource() != null)
			{
				if (nameToLinkMap.put(link.getSource(), link) != null)
				{
					throw new IllegalStateException("Duplicate orm configuration for link '" + link.getSource() + "'");
				}
			}
			if (link.getAlias() != null)
			{
				if (nameToLinkMap.put(link.getAlias(), link) != null)
				{
					throw new IllegalStateException("Duplicate orm configuration for link '" + link.getAlias() + "'");
				}
			}
		}

		IList<Element> entityElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.ENTITY));
		IList<Element> externalEntityElements = xmlConfigUtil.nodesToElements(doc.getElementsByTagName(XmlConstants.EXTERNAL_ENTITY));
		entityElements.addAll(externalEntityElements);
		for (int i = entityElements.size(); i-- > 0;)
		{
			Element entityTag = entityElements.get(i);
			EntityConfig entityConfig = readEntityConfig(entityTag, nameToLinkMap);
			if (localEntities.contains(entityConfig) || externalEntities.contains(entityConfig))
			{
				throw new IllegalStateException("Duplicate orm configuration for entity '" + entityConfig.getEntityType().getName() + "'");
			}
			if (entityConfig.isLocal())
			{
				localEntities.add(entityConfig);
			}
			else
			{
				externalEntities.add(entityConfig);
			}
		}
	}

	protected void validateDocument(Document doc)
	{
		try
		{
			validator.validate(doc);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e, "Error during xml document validation");
		}
	}

	protected LinkConfig readLinkConfig(Element linkTag)
	{
		String source = xmlConfigUtil.getRequiredAttribute(linkTag, XmlConstants.SOURCE);
		LinkConfig link = new LinkConfig(source);

		String cascadeDeleteRaw = xmlConfigUtil.getAttribute(linkTag, XmlConstants.CASCADE_DELETE);
		if (!cascadeDeleteRaw.isEmpty())
		{
			CascadeDeleteDirection cascadeDelete = CascadeDeleteDirection.valueOf(cascadeDeleteRaw.toUpperCase());
			link.setCascadeDeleteDirection(cascadeDelete);
		}

		String alias = xmlConfigUtil.getAttribute(linkTag, XmlConstants.ALIAS);
		if (!alias.isEmpty())
		{
			link.setAlias(alias);
		}

		return link;
	}

	protected ExternalLinkConfig readExternalLinkConfig(Element linkTag)
	{
		LinkConfig link = readLinkConfig(linkTag);
		ExternalLinkConfig eLink = new ExternalLinkConfig(link.getSource());

		String sourceColumn = xmlConfigUtil.getRequiredAttribute(linkTag, XmlConstants.SOURCE_COLUMN);
		eLink.setSourceColumn(sourceColumn);
		String targetMember = xmlConfigUtil.getRequiredAttribute(linkTag, XmlConstants.TARGET_MEMBER);
		eLink.setTargetMember(targetMember);

		eLink.setCascadeDeleteDirection(link.getCascadeDeleteDirection());
		eLink.setAlias(link.getAlias());

		return eLink;
	}

	protected ILinkConfig readIndependentLinkConfig(Element linkTag)
	{
		String alias = xmlConfigUtil.getRequiredAttribute(linkTag, XmlConstants.ALIAS);
		IndependentLinkConfig link = new IndependentLinkConfig(alias);

		String cascadeDeleteRaw = xmlConfigUtil.getAttribute(linkTag, XmlConstants.CASCADE_DELETE);
		if (!cascadeDeleteRaw.isEmpty())
		{
			CascadeDeleteDirection cascadeDelete = CascadeDeleteDirection.valueOf(cascadeDeleteRaw.toUpperCase());
			link.setCascadeDeleteDirection(cascadeDelete);
		}

		String leftStr = xmlConfigUtil.getAttribute(linkTag, XmlConstants.LEFT);
		if (!leftStr.isEmpty())
		{
			Class<?> left = xmlConfigUtil.getTypeForName(leftStr);
			link.setLeft(left);
		}

		String rightStr = xmlConfigUtil.getAttribute(linkTag, XmlConstants.RIGHT);
		if (!rightStr.isEmpty())
		{
			Class<?> right = xmlConfigUtil.getTypeForName(rightStr);
			link.setRight(right);
		}

		return link;
	}

	protected EntityConfig readEntityConfig(Element entityTag, Map<String, ILinkConfig> nameToLinkMap)
	{
		String entityTypeName = xmlConfigUtil.getRequiredAttribute(entityTag, XmlConstants.CLASS);
		try
		{
			Class<?> entityType = xmlConfigUtil.getTypeForName(entityTypeName);
			Class<?> realType = proxyHelper.getRealType(entityType);
			EntityConfig entityConfig = new EntityConfig(entityType, realType);

			final boolean localEntity = !entityTag.getNodeName().equals(XmlConstants.EXTERNAL_ENTITY);
			entityConfig.setLocal(localEntity);

			IMap<String, IList<Element>> attributeMap = null;

			IMap<String, IList<Element>> entityDefs = xmlConfigUtil.childrenToElementMap(entityTag);
			if (entityDefs.containsKey(XmlConstants.TABLE))
			{
				String specifiedTableName = xmlConfigUtil.getRequiredAttribute(entityDefs.get(XmlConstants.TABLE).get(0), XmlConstants.NAME);
				entityConfig.setTableName(specifiedTableName);
			}
			if (entityDefs.containsKey(XmlConstants.PERMISSION_GROUP))
			{
				String permissionGroupName = xmlConfigUtil.getRequiredAttribute(entityDefs.get(XmlConstants.PERMISSION_GROUP).get(0), XmlConstants.NAME);
				entityConfig.setPermissionGroupName(permissionGroupName);
			}
			if (entityDefs.containsKey(XmlConstants.SEQ))
			{
				String sequenceName = xmlConfigUtil.getRequiredAttribute(entityDefs.get(XmlConstants.SEQ).get(0), XmlConstants.NAME);
				entityConfig.setSequenceName(sequenceName);
			}
			if (entityDefs.containsKey(XmlConstants.DESCRIMINATOR))
			{
				String descriminatorName = xmlConfigUtil.getRequiredAttribute(entityDefs.get(XmlConstants.DESCRIMINATOR).get(0), XmlConstants.NAME);
				entityConfig.setDescriminatorName(descriminatorName);
			}

			if (entityDefs.containsKey(XmlConstants.ATTR))
			{
				attributeMap = xmlConfigUtil.toElementMap(entityDefs.get(XmlConstants.ATTR).get(0).getChildNodes());
			}
			boolean versionRequired = true;
			if (attributeMap != null)
			{
				IMap<String, MemberConfig> allIdMemberConfigs = new HashMap<String, MemberConfig>();
				if (attributeMap.containsKey(XmlConstants.ID))
				{
					MemberConfig idMemberConfig = readUniqueMemberConfig(XmlConstants.ID, attributeMap);
					entityConfig.setIdMemberConfig(idMemberConfig);
					allIdMemberConfigs.put(idMemberConfig.getName(), idMemberConfig);
				}
				else if (attributeMap.containsKey(XmlConstants.ID_COMP))
				{
					Element memberElement = attributeMap.get(XmlConstants.ID_COMP).get(0);
					IMemberConfig idMemberConfig = readCompositeMemberConfig(memberElement, allIdMemberConfigs);
					entityConfig.setIdMemberConfig(idMemberConfig);
				}
				else if (!localEntity)
				{
					throw new IllegalArgumentException("ID member name has to be set on external entities");
				}

				if (attributeMap.containsKey(XmlConstants.ALT_ID))
				{
					IList<Element> altIds = attributeMap.get(XmlConstants.ALT_ID);
					for (int j = altIds.size(); j-- > 0;)
					{
						Element memberElement = altIds.get(j);
						MemberConfig memberConfig = readMemberConfig(memberElement);
						memberConfig.setAlternateId(true);
						entityConfig.addMemberConfig(memberConfig);
						allIdMemberConfigs.put(memberConfig.getName(), memberConfig);
					}
				}

				if (attributeMap.containsKey(XmlConstants.ALT_ID_COMP))
				{
					IList<Element> altIdsComp = attributeMap.get(XmlConstants.ALT_ID_COMP);
					for (int j = altIdsComp.size(); j-- > 0;)
					{
						Element memberElement = altIdsComp.get(j);
						CompositeMemberConfig memberConfig = readCompositeMemberConfig(memberElement, allIdMemberConfigs);
						memberConfig.setAlternateId(true);
						entityConfig.addMemberConfig(memberConfig);
					}
				}

				if (attributeMap.containsKey(XmlConstants.VERSION))
				{
					MemberConfig versionMemberConfig = readUniqueMemberConfig(XmlConstants.VERSION, attributeMap);
					entityConfig.setVersionMemberConfig(versionMemberConfig);
				}
				else if (attributeMap.containsKey(XmlConstants.NO_VERSION))
				{
					versionRequired = false;
				}
				else if (!localEntity)
				{
					throw new IllegalArgumentException("Version member name has to be set on external entities");
				}

				if (attributeMap.containsKey(XmlConstants.CREATED_BY))
				{
					MemberConfig createdByMemberConfig = readUniqueMemberConfig(XmlConstants.CREATED_BY, attributeMap);
					entityConfig.setCreatedByMemberConfig(createdByMemberConfig);
				}
				if (attributeMap.containsKey(XmlConstants.CREATED_ON))
				{
					MemberConfig createdOnMemberConfig = readUniqueMemberConfig(XmlConstants.CREATED_ON, attributeMap);
					entityConfig.setCreatedOnMemberConfig(createdOnMemberConfig);
				}
				if (attributeMap.containsKey(XmlConstants.UPDATED_BY))
				{
					MemberConfig updatedByMemberConfig = readUniqueMemberConfig(XmlConstants.UPDATED_BY, attributeMap);
					entityConfig.setUpdatedByMemberConfig(updatedByMemberConfig);
				}
				if (attributeMap.containsKey(XmlConstants.UPDATED_ON))
				{
					MemberConfig updatedOnMemberConfig = readUniqueMemberConfig(XmlConstants.UPDATED_ON, attributeMap);
					entityConfig.setUpdatedOnMemberConfig(updatedOnMemberConfig);
				}

				if (attributeMap.containsKey(XmlConstants.BASIC))
				{
					IList<Element> basicAttrs = attributeMap.get(XmlConstants.BASIC);
					for (int j = basicAttrs.size(); j-- > 0;)
					{
						Element memberElement = basicAttrs.get(j);
						MemberConfig memberConfig = readMemberConfig(memberElement);
						entityConfig.addMemberConfig(memberConfig);
					}
				}

				if (attributeMap.containsKey(XmlConstants.IGNORE))
				{
					IList<Element> ignoreAttrs = attributeMap.get(XmlConstants.IGNORE);
					for (int j = ignoreAttrs.size(); j-- > 0;)
					{
						Element ignoreElement = ignoreAttrs.get(j);
						MemberConfig memberConfig = readMemberConfig(ignoreElement);
						memberConfig.setIgnore(true);
						entityConfig.addMemberConfig(memberConfig);
					}
				}

				if (attributeMap.containsKey(XmlConstants.RELATION))
				{
					IList<Element> relationAttrs = attributeMap.get(XmlConstants.RELATION);
					for (int j = relationAttrs.size(); j-- > 0;)
					{
						Element relationElement = relationAttrs.get(j);
						IRelationConfig relationConfig = readRelationConfig(relationElement, nameToLinkMap);
						entityConfig.addRelationConfig(relationConfig);
					}
				}
			}
			entityConfig.setVersionRequired(versionRequired);

			return entityConfig;
		}
		catch (RuntimeException e)
		{
			throw RuntimeExceptionUtil.mask(e, "Error occured while processing mapping for entity: " + entityTypeName);
		}
	}

	protected MemberConfig readUniqueMemberConfig(String tagName, IMap<String, IList<Element>> attributeMap)
	{
		Element memberElement = attributeMap.get(tagName).get(0);
		MemberConfig memberConfig = readMemberConfig(memberElement);
		return memberConfig;
	}

	protected MemberConfig readMemberConfig(Element memberElement)
	{
		String memberName = xmlConfigUtil.getRequiredAttribute(memberElement, XmlConstants.NAME);
		MemberConfig memberConfig = new MemberConfig(memberName);

		String columnName = xmlConfigUtil.getAttribute(memberElement, XmlConstants.COLUMN);
		if (!columnName.isEmpty())
		{
			memberConfig.setColumnName(columnName);
		}
		String transientValue = xmlConfigUtil.getAttribute(memberElement, XmlConstants.TRANSIENT);
		if (!transientValue.isEmpty())
		{
			memberConfig.setTransient(Boolean.parseBoolean(transientValue));
		}
		String definedByValue = xmlConfigUtil.getAttribute(memberElement, XmlConstants.DEFINED_BY);
		if (!definedByValue.isEmpty())
		{
			memberConfig.setDefinedBy(definedByValue);
		}
		return memberConfig;
	}

	protected CompositeMemberConfig readCompositeMemberConfig(Element memberElement, IMap<String, MemberConfig> allIdMemberConfigs)
	{
		NodeList idFragmentNodes = memberElement.getElementsByTagName(XmlConstants.ID_FRAGMENT);
		IList<Element> idFragments = xmlConfigUtil.nodesToElements(idFragmentNodes);
		MemberConfig[] memberConfigs = new MemberConfig[idFragments.size()];
		for (int i = 0; i < idFragments.size(); i++)
		{
			Element idFragment = idFragments.get(i);
			String memberName = xmlConfigUtil.getRequiredAttribute(idFragment, XmlConstants.NAME);
			MemberConfig memberConfig = allIdMemberConfigs.get(memberName);
			if (memberConfig == null)
			{
				memberConfig = readMemberConfig(idFragment);
				allIdMemberConfigs.put(memberName, memberConfig);
			}
			memberConfigs[i] = memberConfig;
		}
		CompositeMemberConfig compositeMemberConfig = new CompositeMemberConfig(memberConfigs);
		return compositeMemberConfig;
	}

	protected IRelationConfig readRelationConfig(Element relationElement, Map<String, ILinkConfig> nameToLinkMap)
	{
		String relationName = xmlConfigUtil.getRequiredAttribute(relationElement, XmlConstants.NAME);
		String linkName = xmlConfigUtil.getAttribute(relationElement, XmlConstants.LINK);
		ILinkConfig linkConfig = null;
		if (!linkName.isEmpty())
		{
			linkConfig = nameToLinkMap.get(linkName);
		}
		if (linkConfig == null)
		{
			if (log.isInfoEnabled())
			{
				if (!linkName.isEmpty())
				{
					log.info("No LinkConfig found for name '" + linkName + "'. Creating one with default values.");
				}
				else
				{
					log.info("Unconfigured Link found for property '" + relationName + "'. Trying to resolve this later.");
				}
			}
			linkConfig = new LinkConfig(linkName);
		}
		try
		{
			RelationConfig20 relationConfig = new RelationConfig20(relationName, linkConfig);

			String entityIdentifierName = xmlConfigUtil.getAttribute(relationElement, XmlConstants.THIS);
			if (!entityIdentifierName.isEmpty())
			{
				EntityIdentifier entityIdentifier = EntityIdentifier.valueOf(entityIdentifierName.toUpperCase());
				relationConfig.setEntityIdentifier(entityIdentifier);
			}

			return relationConfig;
		}
		catch (RuntimeException e)
		{
			throw new RuntimeException("Error occured while processing relation '" + relationName + "'", e);
		}
	}
}
