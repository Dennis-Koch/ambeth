package de.osthus.ambeth.xml.pending;

public interface IObjectFutureHandlerExtendable
{
	void registerObjectFutureHandler(IObjectFutureHandler objectFutureHandler, Class<?> type);

	void unregisterObjectFutureHandler(IObjectFutureHandler objectFutureHandler, Class<?> type);
}