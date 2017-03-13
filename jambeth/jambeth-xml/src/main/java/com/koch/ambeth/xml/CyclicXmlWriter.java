package com.koch.ambeth.xml;

import java.util.Map.Entry;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessor;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessorRegistry;
import com.koch.ambeth.xml.simple.SimpleXmlWriter;

public class CyclicXmlWriter extends SimpleXmlWriter
{
	@Autowired
	protected ICyclicXmlDictionary xmlDictionary;

	@Autowired
	protected IXmlPostProcessorRegistry xmlPostProcessorRegistry;

	@Override
	protected void writePrefix(IWriter writer)
	{
		super.writePrefix(writer);
		writer.writeOpenElement(xmlDictionary.getRootElement());
	}

	@Override
	protected void writePostfix(IWriter writer)
	{
		writer.writeCloseElement(xmlDictionary.getRootElement());
		super.writePostfix(writer);
	}

	@Override
	protected void postProcess(DefaultXmlWriter writer)
	{
		ILinkedMap<String, IXmlPostProcessor> xmlPostProcessors = xmlPostProcessorRegistry.getXmlPostProcessors();
		LinkedHashMap<String, Object> ppResults = LinkedHashMap.create(xmlPostProcessors.size());
		for (Entry<String, IXmlPostProcessor> entry : xmlPostProcessors)
		{
			String tagName = entry.getKey();
			IXmlPostProcessor xmlPostProcessor = entry.getValue();
			Object result = xmlPostProcessor.processWrite(writer);
			if (result != null)
			{
				ppResults.put(tagName, result);
			}
		}

		if (ppResults.isEmpty())
		{
			return;
		}

		String postProcessElement = xmlDictionary.getPostProcessElement();
		writer.writeStartElement(postProcessElement);
		for (Entry<String, Object> entry : ppResults)
		{
			String tagName = entry.getKey();
			Object result = entry.getValue();

			writer.writeOpenElement(tagName);
			writer.writeObject(result);
			writer.writeCloseElement(tagName);
		}
		writer.writeCloseElement(postProcessElement);
	}
}
