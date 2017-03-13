package com.koch.ambeth.xml;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.xml.postprocess.IPostProcessReader;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessor;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessorRegistry;
import com.koch.ambeth.xml.simple.SimpleXmlReader;

public class CyclicXmlReader extends SimpleXmlReader
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICyclicXmlDictionary xmlDictionary;

	@Autowired
	protected IXmlPostProcessorRegistry xmlPostProcessorRegistry;

	@Override
	protected Object postProcess(Object object, IPostProcessReader pullParserReader)
	{
		handlePostProcessingTag(pullParserReader);

		return super.postProcess(object, pullParserReader);
	}

	@Override
	protected void readPrefix(IReader reader)
	{
		if (!xmlDictionary.getRootElement().equals(reader.getElementName()))
		{
			throw new IllegalStateException("Invalid root element: " + reader);
		}
		reader.nextTag();
	}

	protected void handlePostProcessingTag(IPostProcessReader pullParserReader)
	{
		if (!pullParserReader.isStartTag())
		{
			return;
		}

		String elementName = pullParserReader.getElementName();
		if (!xmlDictionary.getPostProcessElement().equals(elementName))
		{
			throw new IllegalStateException("Only <pp> allowed as second child of root. <" + elementName + "> found.");
		}

		pullParserReader.nextTag();

		while (pullParserReader.isStartTag())
		{
			elementName = pullParserReader.getElementName();
			IXmlPostProcessor xmlPostProcessor = xmlPostProcessorRegistry.getXmlPostProcessor(elementName);
			if (xmlPostProcessor == null)
			{
				throw new IllegalStateException("Post processing tag <" + elementName + "> not supported.");
			}

			xmlPostProcessor.processRead(pullParserReader);

			pullParserReader.moveOverElementEnd();
		}
	}
}