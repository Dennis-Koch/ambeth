package com.koch.ambeth.xml;

/*-
 * #%L
 * jambeth-xml
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
