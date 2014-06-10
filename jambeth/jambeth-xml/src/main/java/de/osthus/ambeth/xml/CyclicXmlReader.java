package de.osthus.ambeth.xml;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.xml.postprocess.IPostProcessReader;
import de.osthus.ambeth.xml.postprocess.IXmlPostProcessor;
import de.osthus.ambeth.xml.postprocess.IXmlPostProcessorRegistry;
import de.osthus.ambeth.xml.simple.SimpleXmlReader;

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