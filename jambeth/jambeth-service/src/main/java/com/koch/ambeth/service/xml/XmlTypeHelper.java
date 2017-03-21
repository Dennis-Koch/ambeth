package com.koch.ambeth.service.xml;

/*-
 * #%L
 * jambeth-service
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
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class XmlTypeHelper implements IXmlTypeHelper
{
	@LogInstance
	private ILogger log;

	protected final HashMap<String, Class<?>> xmlNameToType = new HashMap<String, Class<?>>(0.5f);

	protected final ReentrantLock writeLock = new ReentrantLock();

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public String getXmlName(Class<?> valueObjectType)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sb.append(getXmlNamespace(valueObjectType));
			if (sb.length() > 0)
			{
				sb.append('/');
			}
			sb.append(getXmlTypeName(valueObjectType));

			return sb.toString();
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	@Override
	public String getXmlNamespace(Class<?> valueObjectType)
	{
		Package voPackage = valueObjectType.getPackage();
		XmlSchema packageAnnotation = voPackage.getAnnotation(XmlSchema.class);
		if (packageAnnotation != null)
		{
			return packageAnnotation.namespace();
		}
		else if (log.isWarnEnabled())
		{
			log.warn("No 'XmlSchema' annotation found on package '" + voPackage.getName() + "'");
		}
		return "";
	}

	@Override
	public String getXmlTypeName(Class<?> valueObjectType)
	{
		XmlType typeAnnotation = valueObjectType.getAnnotation(XmlType.class);
		if (typeAnnotation != null)
		{
			return typeAnnotation.name();
		}
		else
		{
			if (log.isWarnEnabled())
			{
				log.warn("No 'XmlType' annotation found on class '" + valueObjectType.getName() + "'");
			}
			return valueObjectType.getSimpleName();
		}
	}

	@Override
	public Class<?> getType(String xmlName)
	{
		ReentrantLock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			Class<?> type = xmlNameToType.get(xmlName);
			if (type != null)
			{
				return type;
			}
			buildXmlNamesToTypeMap();

			type = xmlNameToType.get(xmlName);

			if (type != null)
			{
				return type;
			}
			throw new IllegalArgumentException("One or more of this xml type names are not mappable to an entity type: " + xmlName);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public Class<?>[] getTypes(List<String> xmlNames)
	{
		Class<?>[] types;
		ReentrantLock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			types = getTypesIntern(xmlNames);
			if (types != null)
			{
				return types;
			}
			buildXmlNamesToTypeMap();

			types = getTypesIntern(xmlNames);

			if (types == null)
			{
				String unmappables = findUnmappables(xmlNames);
				throw new IllegalArgumentException("One or more of this xml type names are not mappable to an entity type: " + unmappables);
			}

			return types;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected Class<?>[] getTypesIntern(List<String> xmlNames)
	{
		Class<?>[] types = new Class<?>[xmlNames.size()];

		for (int i = xmlNames.size(); i-- > 0;)
		{
			String xmlName = xmlNames.get(i);
			Class<?> type = xmlNameToType.get(xmlName);
			if (type == null)
			{
				return null;
			}
			types[i] = type;
		}

		return types;
	}

	/**
	 * Just for giving a helpful exception message.
	 * 
	 * @param xmlNames
	 *            All xml type names given.
	 * @return Listing of unresolvable names.
	 */
	protected String findUnmappables(List<String> xmlNames)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		String separator = "";

		for (int i = xmlNames.size(); i-- > 0;)
		{
			String xmlName = xmlNames.get(i);
			Class<?> type = xmlNameToType.get(xmlName);
			if (type != null)
			{
				continue;
			}
			sb.append(separator).append(xmlName);
			separator = ", ";
		}

		return sb.toString();
	}

	protected void buildXmlNamesToTypeMap()
	{
		IMap<String, Class<?>> xmlNameToType = this.xmlNameToType;
		xmlNameToType.clear();
		IList<Class<?>> mappableEntityTypes = entityMetaDataProvider.findMappableEntityTypes();
		for (int i = mappableEntityTypes.size(); i-- > 0;)
		{
			Class<?> entityType = mappableEntityTypes.get(i);
			Class<?> valueObjectType = getUniqueValueObjectType(entityType);
			String xmlName = getXmlName(valueObjectType);
			xmlNameToType.put(xmlName, valueObjectType);
		}
	}

	protected Class<?> getUniqueValueObjectType(Class<?> entityType)
	{
		List<Class<?>> targetValueObjectTypes = entityMetaDataProvider.getValueObjectTypesByEntityType(entityType);
		if (targetValueObjectTypes.size() > 1)
		{
			throw new IllegalStateException("Entity type '" + entityType.getName()
					+ "' has more than 1 mapped value type. Autoresolving value type is not possible. Currently this feature is not supported");
		}
		return targetValueObjectTypes.get(0);
	}
}
