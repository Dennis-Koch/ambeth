package de.osthus.esmeralda.handler;

public interface IStatementHandlerExtendable
{
	void register(IStatementHandlerExtension<?> extension, String key);

	void unregister(IStatementHandlerExtension<?> extension, String key);
}
