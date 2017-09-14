package com.koch.ambeth.xml;

import java.util.Objects;

import com.koch.ambeth.util.IPrintable;

public class XmlTypeKey implements IPrintable, IXmlTypeKey {
	protected final String name;

	protected final String namespace;

	public XmlTypeKey(String name, String namespace) {
		this.name = name;
		this.namespace = namespace;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public int hashCode() {
		if (namespace == null) {
			return name.hashCode();
		}
		return name.hashCode() ^ namespace.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XmlTypeKey)) {
			return false;
		}
		IXmlTypeKey other = (IXmlTypeKey) obj;
		return Objects.equals(name, other.getName())
				&& Objects.equals(namespace, other.getNamespace());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("XmlTypeKey: ").append(name);
		if (namespace != null) {
			sb.append(" ").append(namespace);
		}
	}
}
