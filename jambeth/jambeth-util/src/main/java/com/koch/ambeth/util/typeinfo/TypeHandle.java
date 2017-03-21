package com.koch.ambeth.util.typeinfo;

/*-
 * #%L
 * jambeth-util
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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.koch.ambeth.util.ParamChecker;

@XmlRootElement(name = "TypeHandle", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class TypeHandle
{
	protected static final Map<String, Map<String, Class<?>>> namespaceToElementNameToClassMap = new HashMap<String, Map<String, Class<?>>>();

	@XmlElement(required = true)
	protected String name;

	@XmlElement(required = false)
	protected String namespace;

	@XmlTransient
	protected Class<?> entityType;

	public TypeHandle()
	{
		// Intended blank
	}

	public TypeHandle(Class<?> type)
	{
		ParamChecker.assertParamNotNull(type, "type");
		setEntityType(type);
	}

	public Class<?> getEntityType()
	{
		if (this.entityType == null)
		{
			Map<String, Class<?>> elementNameToClassMap = namespaceToElementNameToClassMap.get(namespace);
			if (elementNameToClassMap == null)
			{
				throw new IllegalStateException("No namespace found '" + namespace + "' to resolve entities");
			}
			this.entityType = elementNameToClassMap.get(name);
			if (this.entityType == null)
			{
				throw new IllegalStateException("No entry name found '" + namespace + ":" + name + "'");
			}
		}
		return entityType;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
		this.name = null;
		this.namespace = null;
		if (entityType != null)
		{
			XmlRootElement xmlRootElement = entityType.getAnnotation(XmlRootElement.class);
			if (xmlRootElement != null)
			{
				this.name = xmlRootElement.name();
				this.namespace = xmlRootElement.namespace();
			}
		}
	}
}
