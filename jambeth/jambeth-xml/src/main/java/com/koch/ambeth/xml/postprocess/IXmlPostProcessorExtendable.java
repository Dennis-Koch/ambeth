package com.koch.ambeth.xml.postprocess;

public interface IXmlPostProcessorExtendable
{
	void registerXmlPostProcessor(IXmlPostProcessor xmlPostProcessor, String tagName);

	void unregisterXmlPostProcessor(IXmlPostProcessor xmlPostProcessor, String tagName);
}
