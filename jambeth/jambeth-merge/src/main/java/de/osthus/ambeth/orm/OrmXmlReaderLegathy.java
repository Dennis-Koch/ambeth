package de.osthus.ambeth.orm;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;
import de.osthus.ambeth.util.xml.XmlConstants;

public class OrmXmlReaderLegathy implements IOrmXmlReader
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	@Override
	public Set<EntityConfig> loadFromDocument(Document doc)
	{
		Set<EntityConfig> entities = new LinkedHashSet<EntityConfig>();
		loadFromDocument(doc, entities, entities);
		return entities;
	}

	@Override
	public void loadFromDocument(Document doc, Set<EntityConfig> localEntities, Set<EntityConfig> externalEntities)
	{
		NodeList entityNodeList = doc.getElementsByTagName(XmlConstants.ENTITY);
		List<Element> entityNodes = xmlConfigUtil.nodesToElements(entityNodeList);

		for (int i = entityNodes.size(); i-- > 0;)
		{
			Element entityNode = entityNodes.get(i);
			EntityConfig entityConfig = readEntityConfig(entityNode);
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

	protected EntityConfig readEntityConfig(Element entityTag)
	{
		String entityTypeName = xmlConfigUtil.getRequiredAttribute(entityTag, XmlConstants.CLASS);
		try
		{
			Class<?> entityType = xmlConfigUtil.getTypeForName(entityTypeName);
			Class<?> realType = proxyHelper.getRealType(entityType);
			EntityConfig entityConfig = new EntityConfig(entityType, realType);

			final boolean localEntity = !xmlConfigUtil.getAttribute(entityTag, XmlConstants.TYPE).equals(XmlConstants.EXTERN);
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
			if (entityDefs.containsKey(XmlConstants.ATTR))
			{
				attributeMap = xmlConfigUtil.toElementMap(entityDefs.get(XmlConstants.ATTR).get(0).getChildNodes());
			}
			boolean versionRequired = true;
			if (attributeMap != null)
			{
				if (attributeMap.containsKey(XmlConstants.ID))
				{
					Element idElement = attributeMap.get(XmlConstants.ID).get(0);
					MemberConfig idMemberConfig = readMemberConfig(idElement);
					entityConfig.setIdMemberConfig(idMemberConfig);
				}
				else if (!localEntity)
				{
					throw new IllegalArgumentException("ID member name has to be set on external entities");
				}

				if (attributeMap.containsKey(XmlConstants.VERSION))
				{
					Element versionElement = attributeMap.get(XmlConstants.VERSION).get(0);
					versionRequired = !Boolean.parseBoolean(xmlConfigUtil.getAttribute(versionElement, XmlConstants.WITHOUT));
					if (versionRequired)
					{
						MemberConfig versionMemberConfig = readMemberConfig(versionElement);
						entityConfig.setVersionMemberConfig(versionMemberConfig);
					}
				}
				else if (!localEntity)
				{
					throw new IllegalArgumentException("Version member name has to be set on external entities");
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

				if (attributeMap.containsKey(XmlConstants.TO_ONE))
				{
					IList<Element> toOneAttrs = attributeMap.get(XmlConstants.TO_ONE);
					for (int j = toOneAttrs.size(); j-- > 0;)
					{
						Element toOneElement = toOneAttrs.get(j);
						RelationConfigLegathy relationConfig = readRelationConfig(toOneElement, localEntity, true);
						entityConfig.addRelationConfig(relationConfig);
					}
				}

				if (attributeMap.containsKey(XmlConstants.TO_MANY))
				{
					IList<Element> toManyAttrs = attributeMap.get(XmlConstants.TO_MANY);
					for (int j = toManyAttrs.size(); j-- > 0;)
					{
						Element toManyElement = toManyAttrs.get(j);
						RelationConfigLegathy relationConfig = readRelationConfig(toManyElement, localEntity, false);
						entityConfig.addRelationConfig(relationConfig);
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
			}
			entityConfig.setVersionRequired(versionRequired);

			return entityConfig;
		}
		catch (RuntimeException e)
		{
			throw new RuntimeException("Error occured while processing mapping for entity: " + entityTypeName, e);
		}
	}

	protected MemberConfig readMemberConfig(Element memberElement)
	{
		String memberName = xmlConfigUtil.getRequiredAttribute(memberElement, XmlConstants.NAME, true);
		String columnName = null;
		Element columnElement = xmlConfigUtil.getChildUnique(memberElement, XmlConstants.COLUMN);
		if (columnElement != null)
		{
			columnName = xmlConfigUtil.getRequiredAttribute(columnElement, XmlConstants.NAME);
		}
		MemberConfig memberConfig = new MemberConfig(memberName, columnName);

		boolean alternateId = Boolean.parseBoolean(xmlConfigUtil.getAttribute(memberElement, XmlConstants.ALT_ID));
		memberConfig.setAlternateId(alternateId);

		return memberConfig;
	}

	protected RelationConfigLegathy readRelationConfig(Element relationElement, boolean localEntity, boolean toOne)
	{
		String relationName = xmlConfigUtil.getRequiredAttribute(relationElement, XmlConstants.NAME, true);
		try
		{
			RelationConfigLegathy relationConfig = new RelationConfigLegathy(relationName, toOne);

			String linkedEntityName = xmlConfigUtil.getRequiredAttribute(relationElement, XmlConstants.TARGET_ENTITY);
			Class<?> linkedEntityType = xmlConfigUtil.getTypeForName(linkedEntityName);
			relationConfig.setLinkedEntityType(linkedEntityType);

			boolean doDelete = Boolean.parseBoolean(xmlConfigUtil.getAttribute(relationElement, XmlConstants.DO_DELETE));
			relationConfig.setDoDelete(doDelete);
			boolean mayDelete = Boolean.parseBoolean(xmlConfigUtil.getAttribute(relationElement, XmlConstants.MAY_DELETE));
			relationConfig.setMayDelete(mayDelete);

			if (localEntity)
			{
				Element joinTableTag = xmlConfigUtil.getChildUnique(relationElement, XmlConstants.JOIN_TABLE);
				if (joinTableTag == null)
				{
					String constraintName = xmlConfigUtil.getAttribute(relationElement, XmlConstants.CONSTRAINT_NAME);
					if (constraintName.isEmpty())
					{
						throw new IllegalArgumentException("Either nested element '" + XmlConstants.JOIN_TABLE + "' or attribute '"
								+ XmlConstants.CONSTRAINT_NAME + "' required to map link");
					}
					relationConfig.setConstraintName(constraintName);
				}
				else
				{
					String joinTableName = xmlConfigUtil.getRequiredAttribute(joinTableTag, XmlConstants.NAME);
					relationConfig.setJoinTableName(joinTableName);

					String fromFieldName = xmlConfigUtil.getChildElementAttribute(joinTableTag, XmlConstants.JOIN_COLUMN, XmlConstants.NAME,
							"Join column name has to be set exactly once");
					relationConfig.setFromFieldName(fromFieldName);
					String toFieldName = xmlConfigUtil.getChildElementAttribute(joinTableTag, XmlConstants.INV_JOIN_COLUMN, XmlConstants.NAME, null);
					relationConfig.setToFieldName(toFieldName);

					String toAttributeName = xmlConfigUtil.getChildElementAttribute(joinTableTag, XmlConstants.INV_JOIN_ATTR, XmlConstants.NAME, null);
					toAttributeName = StringConversionHelper.upperCaseFirst(objectCollector, toAttributeName);
					relationConfig.setToAttributeName(toAttributeName);

					if (toFieldName == null && toAttributeName == null)
					{
						throw new IllegalArgumentException("Inverse join column or attribute name has to be set");
					}
				}
			}

			return relationConfig;
		}
		catch (RuntimeException e)
		{
			throw new RuntimeException("Error occured while processing relation '" + relationName + "'", e);
		}
	}
}
