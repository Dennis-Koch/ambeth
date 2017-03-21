package com.koch.ambeth.merge.config;

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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IValueObjectConfigExtendable;
import com.koch.ambeth.merge.ValueObjectConfig;
import com.koch.ambeth.merge.event.EntityMetaDataAddedEvent;
import com.koch.ambeth.merge.orm.IOrmEntityTypeProvider;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.ValueObjectMemberType;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import com.koch.ambeth.util.xml.IXmlConfigUtil;
import com.koch.ambeth.util.xml.XmlConstants;

public class ValueObjectConfigReader
		implements IEventListener, IDisposableBean, IInitializingBean, IStartingBean {
	private static final String[] memberTagNames = {XmlConstants.BASIC, XmlConstants.RELATION};

	@LogInstance
	private ILogger log;

	protected final Set<IValueObjectConfig> managedValueObjectConfigs =
			new LinkedHashSet<>();

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IValueObjectConfigExtendable valueObjectConfigExtendable;

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	@Property(name = MergeConfigurationConstants.ValueObjectConfigValidationActive,
			defaultValue = "false")
	protected boolean runtimeValidationActive;

	protected String xmlFileName = null;

	protected HashMap<Class<?>, List<Element>> configsToConsume;

	@Autowired
	protected IOrmEntityTypeProvider ormEntityTypeProvider;

	@Override
	public void afterPropertiesSet() throws Throwable {
		if (xmlFileName != null) {
			Document[] docs = xmlConfigUtil.readXmlFiles(xmlFileName);
			ParamChecker.assertNotNull(docs, "docs");
			configsToConsume = readConfig(docs);
		}
	}

	@Override
	public void afterStarted() throws Throwable {
		if (configsToConsume == null) {
			return;
		}
		for (Entry<Class<?>, List<Element>> entry : configsToConsume) {
			Class<?> entityType = entry.getKey();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
			if (metaData == null) {
				if (log.isInfoEnabled()) {
					log.info("Could not resolve entity meta data for '" + entityType.getName() + "'");
				}
			}
		}
	}

	@Override
	public void destroy() {
		for (IValueObjectConfig valueObjectConfig : managedValueObjectConfigs) {
			valueObjectConfigExtendable.unregisterValueObjectConfig(valueObjectConfig);
		}
	}

	@Property(name = ServiceConfigurationConstants.valueObjectFile, mandatory = false)
	public void setFileName(String fileName) {
		if (xmlFileName != null) {
			throw new IllegalArgumentException(
					"ValueObjectConfigReader already configured! Tried to set the config file '" + fileName
							+ "'. File name is already set to '" + xmlFileName + "'");
		}

		xmlFileName = fileName;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Property(name = ServiceConfigurationConstants.valueObjectResource, mandatory = false)
	public void setResourceName(String xmlResourceName) {
		if (xmlFileName != null) {
			throw new IllegalArgumentException(
					"ValueObjectConfigReader already configured! Tried to set the config resource '"
							+ xmlResourceName + "'. Resource name is already set to '" + xmlFileName + "'");
		}

		xmlFileName = xmlResourceName;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) {
		if (!(eventObject instanceof EntityMetaDataAddedEvent)) {
			return;
		}
		if (configsToConsume == null) {
			return;
		}
		for (Class<?> entityType : ((EntityMetaDataAddedEvent) eventObject).getEntityTypes()) {
			List<Element> configs = configsToConsume.get(entityType);
			if (configs == null) {
				continue;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			consumeConfigs(metaData, configs);
		}
	}

	protected HashMap<Class<?>, List<Element>> readConfig(Document[] docs) {
		HashMap<Class<?>, List<Element>> entities = new HashMap<>();
		for (Document doc : docs) {
			doc.normalizeDocument();
			NodeList docEntityNodes = doc.getElementsByTagName(XmlConstants.ENTITY);
			List<Element> docEntities = xmlConfigUtil.nodesToElements(docEntityNodes);
			for (int a = docEntities.size(); a-- > 0;) {
				Element docEntity = docEntities.get(a);
				Class<?> entityType = resolveEntityType(docEntity);
				if (entityType == null) {
					// ignore all entries without a valid entity type mapping
					continue;
				}
				List<Element> list = entities.get(entityType);
				if (list == null) {
					list = new ArrayList<>();
					entities.put(entityType, list);
				}
				list.add(docEntity);
			}
		}
		return entities;
	}

	protected Class<?> resolveEntityType(Element item) {
		Map<String, IList<Element>> configs = xmlConfigUtil.childrenToElementMap(item);
		if (!configs.containsKey(XmlConstants.VALUE_OBJECT)) {
			return null;
		}
		String entityTypeName = xmlConfigUtil.getRequiredAttribute(item, XmlConstants.CLASS);
		return ormEntityTypeProvider.resolveEntityType(entityTypeName);
	}

	protected void consumeConfigs(IEntityMetaData metaData, List<Element> entities) {
		for (int i = entities.size(); i-- > 0;) {
			Element item = entities.get(i);

			Map<String, IList<Element>> configs = xmlConfigUtil.childrenToElementMap(item);
			IList<Element> voConfigs = configs.get(XmlConstants.VALUE_OBJECT);
			for (int j = voConfigs.size(); j-- > 0;) {
				Element voConfig = voConfigs.get(j);

				String valueTypeName = xmlConfigUtil.getRequiredAttribute(voConfig, XmlConstants.CLASS);
				Class<?> valueType = ormEntityTypeProvider.resolveEntityType(valueTypeName);

				boolean exists = false;
				for (IValueObjectConfig conf : managedValueObjectConfigs) {
					if (conf.getValueType().equals(valueType)
							&& conf.getEntityType().equals(metaData.getEntityType())) {
						exists = true;
						break;
					}
				}
				if (exists) {
					continue;
				}

				ValueObjectConfig config = new ValueObjectConfig();
				config.setEntityType(metaData.getEntityType());
				config.setValueType(valueType);

				handleMembers(config, voConfig, metaData);

				managedValueObjectConfigs.add(config);
				valueObjectConfigExtendable.registerValueObjectConfig(config);
			}
		}
	}

	protected void handleMembers(ValueObjectConfig config, Element voConfig,
			IEntityMetaData metaData) {
		IMap<String, IList<Element>> configDetails = xmlConfigUtil.childrenToElementMap(voConfig);
		handleIgnoredMembers(config, configDetails);
		handleMemberMappings(config, configDetails, metaData);
		handlePrimitiveCollections(config, configDetails);
		handleRelations(config, configDetails);
	}

	protected void handleIgnoredMembers(ValueObjectConfig config,
			IMap<String, IList<Element>> configDetails) {
		IList<Element> memberTags = configDetails.get(XmlConstants.IGNORE);
		if (memberTags == null) {
			return;
		}

		for (int j = memberTags.size(); j-- > 0;) {
			Element element = memberTags.get(j);
			String memberName = xmlConfigUtil.getRequiredAttribute(element, XmlConstants.NAME);
			config.setValueObjectMemberType(memberName, ValueObjectMemberType.IGNORE);
		}
	}

	protected void handleMemberMappings(ValueObjectConfig config,
			IMap<String, IList<Element>> configDetails, IEntityMetaData metaData) {
		Class<?> entityType = config.getEntityType();
		Class<?> valueType = config.getValueType();

		Map<String, IPropertyInfo> entityPropertyMap = propertyInfoProvider.getPropertyMap(entityType);
		Map<String, IPropertyInfo> valuePropertyMap = propertyInfoProvider.getPropertyMap(valueType);

		for (int i = memberTagNames.length; i-- > 0;) {
			String memberTagName = memberTagNames[i];

			IList<Element> memberTags = configDetails.get(memberTagName);
			if (memberTags == null) {
				continue;
			}

			for (int j = memberTags.size(); j-- > 0;) {
				Element element = memberTags.get(j);
				String memberName = xmlConfigUtil.getRequiredAttribute(element, XmlConstants.NAME);

				if (config.isIgnoredMember(memberName)) {
					continue;
				}
				if (runtimeValidationActive
						&& !isPropertyResolvable(valueType, valuePropertyMap, memberName, null)) {
					throw new IllegalStateException(
							"Value type property '" + valueType.getName() + "." + memberName + "' not found");
				}

				boolean holdsListType = xmlConfigUtil.attributeIsTrue(element, XmlConstants.LIST_TYPE);
				if (holdsListType) {
					config.addListTypeMember(memberName);
				}

				String entityMemberName = xmlConfigUtil.getAttribute(element, XmlConstants.NAME_IN_ENTITY);
				if (entityMemberName.isEmpty()) {
					entityMemberName = memberName;
				}
				else {
					config.putValueObjectMemberName(entityMemberName, memberName);
				}
				if (runtimeValidationActive
						&& !isPropertyResolvable(entityType, entityPropertyMap, entityMemberName, metaData)) {
					throw new IllegalStateException(
							"Entity type property '" + entityType.getName() + "." + entityMemberName
									+ "' not found while configuring value type '" + valueType.getName() + "'");
				}
			}
		}
	}

	protected boolean isPropertyResolvable(Class<?> type, Map<String, IPropertyInfo> propertyMap,
			String memberName, IEntityMetaData metaData) {
		if (metaData != null && metaData.getMemberByName(memberName) == null) {
			return false;
		}
		if (propertyMap == null) {
			propertyMap = propertyInfoProvider.getPropertyMap(type);
		}
		if (propertyMap.containsKey(memberName)) {
			return true;
		}
		String[] memberPath = memberName.split("\\.");
		if (memberPath.length == 0) {
			return false;
		}
		IPropertyInfo propertyInfo = propertyMap.get(memberPath[0]);
		if (propertyInfo == null) {
			return false;
		}
		String remainingMemberName = memberName.substring(memberPath[0].length() + 1);
		return isPropertyResolvable(propertyInfo.getPropertyType(), null, remainingMemberName,
				metaData);
	}

	protected void handlePrimitiveCollections(ValueObjectConfig config,
			IMap<String, IList<Element>> configDetails) {
		IList<Element> memberTags = configDetails.get(XmlConstants.BASIC);
		if (memberTags == null) {
			return;
		}

		for (int j = memberTags.size(); j-- > 0;) {
			Element element = memberTags.get(j);
			String memberName = xmlConfigUtil.getRequiredAttribute(element, XmlConstants.NAME);
			if (config.isIgnoredMember(memberName)) {
				continue;
			}
			config.setValueObjectMemberType(memberName, ValueObjectMemberType.BASIC);

			String targetElementType =
					xmlConfigUtil.getAttribute(element, XmlConstants.TARGET_ELEMENT_TYPE);
			if (targetElementType.isEmpty()) {
				continue;
			}
			Class<?> elementType = xmlConfigUtil.getTypeForName(targetElementType);
			config.putMemberType(memberName, elementType);
		}
	}

	protected void handleRelations(ValueObjectConfig config,
			IMap<String, IList<Element>> configDetails) {
		IList<Element> elementTypes = configDetails.get(XmlConstants.RELATION);
		if (elementTypes == null) {
			return;
		}

		for (int i = elementTypes.size(); i-- > 0;) {
			Element element = elementTypes.get(i);
			String memberName = xmlConfigUtil.getRequiredAttribute(element, XmlConstants.NAME);

			if (config.isIgnoredMember(memberName)) {
				continue;
			}

			config.setValueObjectMemberType(memberName, ValueObjectMemberType.RELATION);

			boolean holdsListType = xmlConfigUtil.attributeIsTrue(element, XmlConstants.LIST_TYPE);
			if (holdsListType) {
				config.addListTypeMember(memberName);
			}

			String elementTypeName =
					xmlConfigUtil.getAttribute(element, XmlConstants.TARGET_VALUE_OBJECT);
			if (!elementTypeName.isEmpty()) {
				Class<?> elementType = xmlConfigUtil.getTypeForName(elementTypeName);
				config.putMemberType(memberName, elementType);
			}
		}
	}
}
