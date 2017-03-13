package com.koch.ambeth.xml.postprocess;

import com.koch.ambeth.util.collections.ILinkedMap;

public interface IXmlPostProcessorRegistry
{
	IXmlPostProcessor getXmlPostProcessor(String tagName);

	ILinkedMap<String, IXmlPostProcessor> getXmlPostProcessors();
}
