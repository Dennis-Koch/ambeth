package de.osthus.ambeth.xml.pending;

public interface IObjectFutureHandlerRegistry
{
	IObjectFutureHandler getObjectFutureHandler(Class<?> type);
}
