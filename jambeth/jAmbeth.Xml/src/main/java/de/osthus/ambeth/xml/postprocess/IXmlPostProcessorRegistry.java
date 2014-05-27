package de.osthus.ambeth.xml.postprocess;

import de.osthus.ambeth.collections.ILinkedMap;

public interface IXmlPostProcessorRegistry
{
	IXmlPostProcessor getXmlPostProcessor(String tagName);

	ILinkedMap<String, IXmlPostProcessor> getXmlPostProcessors();
}
