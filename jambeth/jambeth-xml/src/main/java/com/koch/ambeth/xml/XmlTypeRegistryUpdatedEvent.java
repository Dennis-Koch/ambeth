package com.koch.ambeth.xml;

import com.koch.ambeth.service.metadata.IDTOType;
import com.koch.ambeth.util.IImmutableType;

public class XmlTypeRegistryUpdatedEvent implements IImmutableType, IDTOType {
	public static enum EventType {
		ADDED, REMOVED
	}

	private final EventType eventType;

	private final Class<?> xmlType;

	private String name;

	private String namespace;

	public XmlTypeRegistryUpdatedEvent(EventType eventType, Class<?> xmlType, String name,
			String namespace) {
		this.eventType = eventType;
		this.xmlType = xmlType;
		this.name = name;
		this.namespace = namespace;
	}

	public EventType getEventType() {
		return eventType;
	}

	public Class<?> getXmlType() {
		return xmlType;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	@Override
	public String toString() {
		return getEventType() + " " + getXmlType().getName();
	}
}
