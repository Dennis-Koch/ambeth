package de.osthus.ambeth.xml;

import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IPrintable;

public class XmlTypeKey implements IPrintable, IXmlTypeKey
{
	protected final String name;

	protected final String namespace;

	public XmlTypeKey(String name, String namespace)
	{
		this.name = name;
		this.namespace = namespace;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getNamespace()
	{
		return namespace;
	}

	@Override
	public int hashCode()
	{
		if (this.namespace == null)
		{
			return this.name.hashCode();
		}
		return this.name.hashCode() ^ this.namespace.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof XmlTypeKey))
		{
			return false;
		}
		IXmlTypeKey other = (IXmlTypeKey) obj;
		return EqualsUtil.equals(this.name, other.getName()) && EqualsUtil.equals(this.namespace, other.getNamespace());
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append("XmlTypeKey: ").append(this.name);
		if (this.namespace != null)
		{
			sb.append(" ").append(this.namespace);
		}
	}
}
