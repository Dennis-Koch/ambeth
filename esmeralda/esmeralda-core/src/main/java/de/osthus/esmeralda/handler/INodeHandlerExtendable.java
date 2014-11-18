package de.osthus.esmeralda.handler;

public interface INodeHandlerExtendable
{
	void register(INodeHandlerExtension extension, String key);

	void unregister(INodeHandlerExtension extension, String key);
}
