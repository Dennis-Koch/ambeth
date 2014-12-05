package de.osthus.esmeralda.handler;

public interface IExpressionHandlerExtendable
{
	void register(IExpressionHandler expressionHandler, String key);

	void unregister(IExpressionHandler expressionHandler, String key);
}
