package de.osthus.ambeth.xml.postprocess;

public interface IXmlPostProcessor
{
	Object processWrite(IPostProcessWriter writer);

	void processRead(IPostProcessReader reader);
}
