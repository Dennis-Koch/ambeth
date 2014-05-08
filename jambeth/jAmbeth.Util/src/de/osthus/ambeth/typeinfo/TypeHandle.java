package de.osthus.ambeth.typeinfo;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.osthus.ambeth.util.ParamChecker;

@XmlRootElement(name = "TypeHandle", namespace = "http://schemas.osthus.de/Ambeth")
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
