package de.osthus.ambeth.xml;

public interface INameBasedHandlerExtendable
{
	void registerNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName);

	void unregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName);
}
