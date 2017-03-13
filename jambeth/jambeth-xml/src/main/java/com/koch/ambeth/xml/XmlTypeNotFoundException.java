package com.koch.ambeth.xml;

public class XmlTypeNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1834212749861874120L;

	private final String xmlType;

	private final String namespace;

	public XmlTypeNotFoundException(String xmlType, String ns)
	{
		super("No type found: Name=" + xmlType + " Namespace=" + ns);
		this.xmlType = xmlType;
		this.namespace = ns;
	}

	public String getXmlType()
	{
		return xmlType;
	}

	public String getNamespace()
	{
		return namespace;
	}
}
