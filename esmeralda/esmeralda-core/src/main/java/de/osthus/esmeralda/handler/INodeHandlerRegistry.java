package de.osthus.esmeralda.handler;

public interface INodeHandlerRegistry
{
	INodeHandlerExtension get(String key);
}
