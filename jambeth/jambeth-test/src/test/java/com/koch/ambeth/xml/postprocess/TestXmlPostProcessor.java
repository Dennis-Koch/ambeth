package com.koch.ambeth.xml.postprocess;

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.xml.postprocess.IPostProcessReader;
import com.koch.ambeth.xml.postprocess.IPostProcessWriter;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessor;

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
