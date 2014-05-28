package de.osthus.ambeth.xml;

public interface ITypeBasedHandlerExtendable
{
	void registerElementHandler(ITypeBasedHandler elementHandler, Class<?> type);

	void unregisterElementHandler(ITypeBasedHandler elementHandler, Class<?> type);
}
