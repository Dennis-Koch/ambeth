package de.osthus.ambeth.xml.postprocess;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;

public class TestXmlPostProcessor implements IXmlPostProcessor
{
	public final IList<String> handledTags = new ArrayList<String>();

	@Override
	public Object processWrite(IPostProcessWriter writer)
	{
		return "";
	}

	@Override
	public void processRead(IPostProcessReader reader)
	{
		String elementName = reader.getElementName();
		handledTags.add(elementName);
	}
}
